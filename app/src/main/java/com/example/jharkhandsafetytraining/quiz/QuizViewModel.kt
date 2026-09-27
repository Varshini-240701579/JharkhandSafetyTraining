package com.example.jharkhandsafetytraining.quiz

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jharkhandsafetytraining.common.CertificateService
import com.example.jharkhandsafetytraining.common.SessionManager
import com.example.jharkhandsafetytraining.data.AppDatabase
import com.example.jharkhandsafetytraining.data.Certificate
import com.example.jharkhandsafetytraining.data.ModuleProgress
import com.example.jharkhandsafetytraining.data.QuizAttempt
import com.example.jharkhandsafetytraining.data.User
import kotlinx.coroutines.launch

class QuizViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.getInstance(app)
    private val trainingDao = db.trainingDao()
    private val userDao = db.userDao()
    private val session = SessionManager(app)
    private val quizRepository = QuizRepository(app)

    var quizBank by mutableStateOf<QuizBank?>(null)
        private set

    var quizResult by mutableStateOf<QuizResult?>(null)
        private set

    var loadError by mutableStateOf<String?>(null)
        private set

    var isSaving by mutableStateOf(false)
        private set

    var allModulesCompleted by mutableStateOf(false)
        private set

    var currentUser by mutableStateOf<User?>(null)
        private set

    var moduleProgressMap by mutableStateOf<Map<String, ModuleProgress>>(emptyMap())
        private set

    var recentAttempts by mutableStateOf<List<QuizAttempt>>(emptyList())
        private set

    init {
        refreshUserAndProgress()
    }

    fun refreshUserAndProgress() {
        viewModelScope.launch {
            val userId = session.getUserId() ?: return@launch
            currentUser = userDao.findById(userId)
            val progressList = trainingDao.getProgress(userId)
            moduleProgressMap = progressList.associateBy { it.moduleId.uppercase() }
            val passedModules = progressList.filter { it.quizPassed }.map { it.moduleId.uppercase() }.toSet()
            allModulesCompleted = CertificateService.hasPassedAllRequiredModules(passedModules)
        }
    }

    fun loadQuiz(moduleId: String) {
        loadError = null
        quizResult = null
        try {
            quizBank = quizRepository.load(moduleId)
        } catch (e: Exception) {
            quizBank = null
            loadError = "Quiz for $moduleId could not be loaded: ${e.localizedMessage}"
        }
        viewModelScope.launch {
            val userId = session.getUserId() ?: 1L
            currentUser = userDao.findById(userId)
            recentAttempts = trainingDao.getAttemptsForModule(userId, moduleId.uppercase())
        }
    }

    fun submitQuiz(moduleId: String, answers: Map<String, Int>, onSaved: (QuizResult) -> Unit = {}) {
        val bank = quizBank ?: return
        val normalizedModuleId = moduleId.uppercase()
        val result = QuizEngine.score(bank, answers)
        quizResult = result
        isSaving = true

        viewModelScope.launch {
            try {
                val userId = session.getUserId() ?: 1L
                val user = userDao.findById(userId)
                currentUser = user
                val now = System.currentTimeMillis()

                // 1. Always record the QuizAttempt (passed or failed) in Room
                trainingDao.insertAttempt(
                    QuizAttempt(
                        userId = userId,
                        moduleId = normalizedModuleId,
                        score = result.score,
                        total = result.total,
                        passed = result.passed,
                        attemptedAt = now,
                        synced = false
                    )
                )

                // 2. If score >= passing threshold, mark ModuleProgress(quizPassed = true) in Room
                if (result.passed) {
                    trainingDao.markQuizPassed(userId = userId, moduleId = normalizedModuleId)

                    val updatedProgress = trainingDao.getProgress(userId)
                    moduleProgressMap = updatedProgress.associateBy { it.moduleId.uppercase() }

                    val passedModules = updatedProgress
                        .filter { it.quizPassed }
                        .map { it.moduleId.uppercase() }
                        .toSet()

                    val workerName = user?.name ?: "Worker #$userId"
                    val workerPhone = user?.phone ?: "N/A"
                    val workerRole = user?.role ?: "WORKER"

                    // Issue module certificate if not yet issued for this module
                    val existingModCert = trainingDao.getCertificateForModule(userId, normalizedModuleId)
                    if (existingModCert == null) {
                        val modSignedPayload = CertificateService.generateSignedPayload(
                            userId = userId,
                            workerName = workerName,
                            workerPhone = workerPhone,
                            role = workerRole,
                            moduleId = normalizedModuleId,
                            modulesCompleted = passedModules.toList(),
                            issuedAt = now
                        )
                        trainingDao.insertCertificate(
                            Certificate(
                                userId = userId,
                                moduleId = normalizedModuleId,
                                issuedAt = now,
                                signedPayload = modSignedPayload,
                                synced = false
                            )
                        )
                    }

                    // 3. When all required modules are passed, generate master HMAC-SHA256 certificate
                    val allPassed = CertificateService.hasPassedAllRequiredModules(passedModules)
                    allModulesCompleted = allPassed
                    if (allPassed) {
                        val existingAllCert = trainingDao.getCertificateForModule(userId, "ALL")
                        if (existingAllCert == null) {
                            val allSignedPayload = CertificateService.generateSignedPayload(
                                userId = userId,
                                workerName = workerName,
                                workerPhone = workerPhone,
                                role = workerRole,
                                moduleId = "ALL",
                                modulesCompleted = CertificateService.REQUIRED_MODULES,
                                issuedAt = now
                            )
                            trainingDao.insertCertificate(
                                Certificate(
                                    userId = userId,
                                    moduleId = "ALL",
                                    issuedAt = now,
                                    signedPayload = allSignedPayload,
                                    synced = false
                                )
                            )
                        }
                    }
                }

                recentAttempts = trainingDao.getAttemptsForModule(userId, normalizedModuleId)
            } finally {
                isSaving = false
                onSaved(result)
            }
        }
    }

    fun resetForRetry() {
        quizResult = null
    }

    fun markArSimulationCompleted(moduleId: String) {
        viewModelScope.launch {
            val userId = session.getUserId() ?: 1L
            trainingDao.markArCompleted(userId, moduleId.uppercase())
            refreshUserAndProgress()
        }
    }
}
