package com.ribuufing.bloodapp.feature.form.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ribuufing.bloodapp.core.manager.AuthManager
import com.ribuufing.bloodapp.feature.form.domain.response.GetQuestionsResponse
import com.ribuufing.bloodapp.feature.form.domain.usecase.GetQuestionsUseCase
import com.ribuufing.bloodapp.feature.form.domain.usecase.CreateFormUseCase
import com.ribuufing.bloodapp.feature.form.domain.usecase.GetFormUseCase
import com.ribuufing.bloodapp.feature.form.domain.usecase.UpdateFormUseCase
import com.ribuufing.bloodapp.feature.form.data.request.CreateFormRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import org.json.JSONObject
import java.util.Iterator

@HiltViewModel
class FormViewModel @Inject constructor(
    private val questionsUseCase: GetQuestionsUseCase,
    private val createFormUseCase: CreateFormUseCase,
    private val getFormUseCase: GetFormUseCase,
    private val updateFormUseCase: UpdateFormUseCase,
    private val authManager: AuthManager
) : ViewModel() {
    private val _formQuestions = MutableStateFlow<List<GetQuestionsResponse>>(emptyList())
    val formQuestions: StateFlow<List<GetQuestionsResponse>> = _formQuestions.asStateFlow()

    private val _formAnswers = MutableStateFlow<Map<Int, String>>(emptyMap())
    val formAnswers: StateFlow<Map<Int, String>> = _formAnswers.asStateFlow()

    private val _formSubmitState = MutableStateFlow<FormSubmitState>(FormSubmitState.Initial)
    val formSubmitState: StateFlow<FormSubmitState> = _formSubmitState.asStateFlow()

    private val _formScreenNavigationTrigger = MutableStateFlow(false)
    val formScreenNavigationTrigger: StateFlow<Boolean> = _formScreenNavigationTrigger.asStateFlow()

    // Form görüntüleme için state
    private val _formViewState = MutableStateFlow(FormViewState())
    val formViewState: StateFlow<FormViewState> = _formViewState.asStateFlow()

    // Edit mode state
    private val _editMode = MutableStateFlow(false)
    val editMode: StateFlow<Boolean> = _editMode.asStateFlow()

    // Update işlemi için state
    private val _updateState = MutableStateFlow<FormUpdateState>(FormUpdateState.Initial)
    val updateState: StateFlow<FormUpdateState> = _updateState.asStateFlow()

    fun loadForm() {
        _formViewState.value = FormViewState(loading = true)
        viewModelScope.launch {
            try {
                val result = getFormUseCase()
                if (result.success && result.response != null) {
                    authManager.formId = result.response.formId.toString()
                    _formViewState.value = FormViewState(form = result.response)
                } else {
                    createNewForm()
                }
            } catch (e: Exception) {
                createNewForm()
            }
        }
    }

    private fun createNewForm() {
        viewModelScope.launch {
            try {
                // Boş bir form oluştur
                val request = CreateFormRequest()
                val response = createFormUseCase(request)
                if (response.success && response.response != null) {
                    authManager.formId = response.response.formId.toString()
                    // Yeni form oluşturuldu, şimdi düzenleme moduna geç
                    startFormFlow()
                    _formViewState.value = FormViewState(loading = false)
                } else {
                    _formViewState.value = FormViewState(error = response.resultMessage ?: "Form oluşturulamadı.")
                }
            } catch (e: Exception) {
                _formViewState.value = FormViewState(error = e.message ?: "Bilinmeyen hata.")
            }
        }
    }

    fun startFormFlow() {
        viewModelScope.launch {
            _formSubmitState.value = FormSubmitState.Initial
            try {
                val response = questionsUseCase()
                if (response.success && response.response != null) {
                    _formQuestions.value = response.response
                    _formScreenNavigationTrigger.value = true
                } else {
                    _formQuestions.value = emptyList()
                    _formSubmitState.value = FormSubmitState.Error(response.resultMessage ?: "Form questions error")
                }
            } catch (e: Exception) {
                _formQuestions.value = emptyList()
                _formSubmitState.value = FormSubmitState.Error(e.message ?: "Form questions exception")
            }
        }
    }

    fun updateFormAnswer(questionId: Int, answer: String) {
        _formAnswers.value = _formAnswers.value.toMutableMap().apply { put(questionId, answer) }
    }

    fun submitForm() {
        viewModelScope.launch {
            _formSubmitState.value = FormSubmitState.Loading
            try {
                val answers = _formAnswers.value
                val questions = _formQuestions.value
                val request = CreateFormRequest(
                    ONAM_FORMU_OKUNDUMU = answers[questions.find { it.key == "ONAM_FORMU_OKUNDUMU" }?.value]?.toBooleanStrictOrNull(),
                    SAGLIKLI_MI = answers[questions.find { it.key == "SAGLIKLI_MI" }?.value]?.toBooleanStrictOrNull(),
                    TEHLIKELI_HOBI_VAR_MI = answers[questions.find { it.key == "TEHLIKELI_HOBI_VAR_MI" }?.value]?.toBooleanStrictOrNull(),
                    DAH_ONCE_GERI_CEVRILDINIZ_MI = answers[questions.find { it.key == "DAH_ONCE_GERI_CEVRILDINIZ_MI" }?.value]?.toBooleanStrictOrNull(),
                    ILAC_KULLANIYOR_MUSUNUZ = answers[questions.find { it.key == "ILAC_KULLANIYOR_MUSUNUZ" }?.value]?.toBooleanStrictOrNull(),
                    ENFEKSIYON_ILAC_ALIMI = answers[questions.find { it.key == "ENFEKSIYON_ILAC_ALIMI" }?.value]?.toBooleanStrictOrNull(),
                    AGRI_KESECI_ALIMI = answers[questions.find { it.key == "AGRI_KESECI_ALIMI" }?.value]?.toBooleanStrictOrNull(),
                    ALERJI_TEDAVISI = answers[questions.find { it.key == "ALERJI_TEDAVISI" }?.value]?.toBooleanStrictOrNull(),
                    DIGER_ILAC_KULLANIMI = answers[questions.find { it.key == "DIGER_ILAC_KULLANIMI" }?.value]?.toBooleanStrictOrNull(),
                    DIS_TEDAVISI = answers[questions.find { it.key == "DIS_TEDAVISI" }?.value]?.toBooleanStrictOrNull(),
                    ISHAL = answers[questions.find { it.key == "ISHAL" }?.value]?.toBooleanStrictOrNull(),
                    ASI_OLUNDU_MU = answers[questions.find { it.key == "ASI_OLUNDU_MU" }?.value]?.toBooleanStrictOrNull(),
                    KRONIK_HASTALIK = answers[questions.find { it.key == "KRONIK_HASTALIK" }?.value]?.toBooleanStrictOrNull(),
                    PARA_KARSILIGI_ILISKI = answers[questions.find { it.key == "PARA_KARSILIGI_ILISKI" }?.value]?.toBooleanStrictOrNull(),
                    FRENGI_GONORE = answers[questions.find { it.key == "FRENGI_GONORE" }?.value]?.toBooleanStrictOrNull(),
                    AIDS_HASTALIGI = answers[questions.find { it.key == "AIDS_HASTALIGI" }?.value]?.toBooleanStrictOrNull(),
                    AIDS_HASTASI_ILE_ILISKI = answers[questions.find { it.key == "AIDS_HASTASI_ILE_ILISKI" }?.value]?.toBooleanStrictOrNull(),
                    KAN_ALAN_KISI_ILE_ILISKI = answers[questions.find { it.key == "KAN_ALAN_KISI_ILE_ILISKI" }?.value]?.toBooleanStrictOrNull(),
                    UYUSTURUCU_KULLANIMI = answers[questions.find { it.key == "UYUSTURUCU_KULLANIMI" }?.value]?.toBooleanStrictOrNull(),
                    HORMON_ILAC_KULLANIMI = answers[questions.find { it.key == "HORMON_ILAC_KULLANIMI" }?.value]?.toBooleanStrictOrNull(),
                    AMELIYAT_ENDOSKOPI = answers[questions.find { it.key == "AMELIYAT_ENDOSKOPI" }?.value]?.toBooleanStrictOrNull(),
                    KALP_AKCIGER_HASTALIK = answers[questions.find { it.key == "KALP_AKCIGER_HASTALIK" }?.value]?.toBooleanStrictOrNull(),
                    NOBET_EPILEPSI_FELC = answers[questions.find { it.key == "NOBET_EPILEPSI_FELC" }?.value]?.toBooleanStrictOrNull(),
                    KANSER_TEDAVISI = answers[questions.find { it.key == "KANSER_TEDAVISI" }?.value]?.toBooleanStrictOrNull(),
                    SEKER_ROMATIZMA = answers[questions.find { it.key == "SEKER_ROMATIZMA" }?.value]?.toBooleanStrictOrNull(),
                    KAN_HASTALIGI = answers[questions.find { it.key == "KAN_HASTALIGI" }?.value]?.toBooleanStrictOrNull(),
                    SITMA_TUBERKULOZ = answers[questions.find { it.key == "SITMA_TUBERKULOZ" }?.value]?.toBooleanStrictOrNull(),
                    HEPATIT_TASIYICILIK = answers[questions.find { it.key == "HEPATIT_TASIYICILIK" }?.value]?.toBooleanStrictOrNull(),
                    HEPATITLI_ILISKI = answers[questions.find { it.key == "HEPATITLI_ILISKI" }?.value]?.toBooleanStrictOrNull(),
                    TOKSOPLAZMA = answers[questions.find { it.key == "TOKSOPLAZMA" }?.value]?.toBooleanStrictOrNull(),
                    BELIRLI_ULKELERDE_BULUNDUNUZ_MU = answers[questions.find { it.key == "BELIRLI_ULKELERDE_BULUNDUNUZ_MU" }?.value]?.toBooleanStrictOrNull(),
                    INGILTERE_KUZAY_IRLANDA = answers[questions.find { it.key == "INGILTERE_KUZAY_IRLANDA" }?.value]?.toBooleanStrictOrNull(),
                    DIGER_ULKELER = answers[questions.find { it.key == "DIGER_ULKELER" }?.value]?.toBooleanStrictOrNull(),
                    DELI_DANA_HASTALIGI = answers[questions.find { it.key == "DELI_DANA_HASTALIGI" }?.value]?.toBooleanStrictOrNull(),
                    BEYIN_ZARI_KORNEA_NAKLI = answers[questions.find { it.key == "BEYIN_ZARI_KORNEA_NAKLI" }?.value]?.toBooleanStrictOrNull(),
                    KAN_ORGAN_NAKLI = answers[questions.find { it.key == "KAN_ORGAN_NAKLI" }?.value]?.toBooleanStrictOrNull(),
                    BASKASININ_KANI_ILE_TEMAS = answers[questions.find { it.key == "BASKASININ_KANI_ILE_TEMAS" }?.value]?.toBooleanStrictOrNull(),
                    DOVME_ESTETIK_MUDAHALE = answers[questions.find { it.key == "DOVME_ESTETIK_MUDAHALE" }?.value]?.toBooleanStrictOrNull(),
                    KUDUZ_ASISI = answers[questions.find { it.key == "KUDUZ_ASISI" }?.value]?.toBooleanStrictOrNull(),
                    TUTUKLULUK = answers[questions.find { it.key == "TUTUKLULUK" }?.value]?.toBooleanStrictOrNull(),
                    KAN_BAGISI_ERKEK = answers[questions.find { it.key == "KAN_BAGISI_ERKEK" }?.value]?.toBooleanStrictOrNull(),
                    ERKEK_ERKEGE_ILISKI_KADIN_HAMILELIK = answers[questions.find { it.key == "ERKEK_ERKEGE_ILISKI_KADIN_HAMILELIK" }?.value]?.toBooleanStrictOrNull(),
                )
                val response = createFormUseCase(request)
                if (response.success) {
                    _formSubmitState.value = FormSubmitState.Success
                } else {
                    _formSubmitState.value = FormSubmitState.Error(response.resultMessage ?: "Form submit error")
                }
            } catch (e: Exception) {
                _formSubmitState.value = FormSubmitState.Error(e.message ?: "Form submit exception")
            }
        }
    }

    fun closeFormDialog() {
        _formAnswers.value = emptyMap()
        _formQuestions.value = emptyList()
        _formSubmitState.value = FormSubmitState.Initial
    }

    fun onFormScreenNavigated() {
        _formScreenNavigationTrigger.value = false
    }

    fun setEditMode(enabled: Boolean) {
        _editMode.value = enabled
    }

    fun loadFormForEdit() {
        viewModelScope.launch {
            _formViewState.value = _formViewState.value.copy(loading = true)
            try {
                val formResult = getFormUseCase()
                authManager.formId = formResult.response?.formId.toString()
                
                if (formResult.success && formResult.response != null) {
                    val form = formResult.response
                    val formDataJson = form.formData
                    val answersMap = parseFormDataToAnswers(formDataJson)
                    
                    // Map answers to questions
                    val questions = _formQuestions.value
                    val answers = questions.associate { q ->
                        q.value to (answersMap[q.key] ?: "")
                    }
                    
                    _formAnswers.value = answers.filterKeys { it != null } as Map<Int, String>
                    _formViewState.value = FormViewState(form = form, questionsLoaded = true)
                } else {
                    _formViewState.value = _formViewState.value.copy(
                        loading = false,
                        error = formResult.resultMessage ?: "Form yüklenemedi."
                    )
                }
            } catch (e: Exception) {
                _formViewState.value = _formViewState.value.copy(
                    loading = false,
                    error = e.message ?: "Bilinmeyen hata."
                )
            }
        }
    }

    fun loadQuestions() {
        viewModelScope.launch {
            try {
                val response = questionsUseCase()
                if (response.success && response.response != null) {
                    _formQuestions.value = response.response
                    _formViewState.value = _formViewState.value.copy(questionsLoaded = true)
                } else {
                    _formViewState.value = FormViewState(error = response.resultMessage ?: "Sorular yüklenemedi.")
                }
            } catch (e: Exception) {
                _formViewState.value = FormViewState(error = e.message ?: "Sorular yüklenirken hata oluştu.")
            }
        }
    }

    fun updateForm() {
        viewModelScope.launch {
            _updateState.value = FormUpdateState.Loading
            try {
                val answers = _formAnswers.value
                val questions = _formQuestions.value
                val request = CreateFormRequest(
                    ONAM_FORMU_OKUNDUMU = answers[questions.find { it.key == "ONAM_FORMU_OKUNDUMU" }?.value]?.toBooleanStrictOrNull(),
                    SAGLIKLI_MI = answers[questions.find { it.key == "SAGLIKLI_MI" }?.value]?.toBooleanStrictOrNull(),
                    TEHLIKELI_HOBI_VAR_MI = answers[questions.find { it.key == "TEHLIKELI_HOBI_VAR_MI" }?.value]?.toBooleanStrictOrNull(),
                    DAH_ONCE_GERI_CEVRILDINIZ_MI = answers[questions.find { it.key == "DAH_ONCE_GERI_CEVRILDINIZ_MI" }?.value]?.toBooleanStrictOrNull(),
                    ILAC_KULLANIYOR_MUSUNUZ = answers[questions.find { it.key == "ILAC_KULLANIYOR_MUSUNUZ" }?.value]?.toBooleanStrictOrNull(),
                    ENFEKSIYON_ILAC_ALIMI = answers[questions.find { it.key == "ENFEKSIYON_ILAC_ALIMI" }?.value]?.toBooleanStrictOrNull(),
                    AGRI_KESECI_ALIMI = answers[questions.find { it.key == "AGRI_KESECI_ALIMI" }?.value]?.toBooleanStrictOrNull(),
                    ALERJI_TEDAVISI = answers[questions.find { it.key == "ALERJI_TEDAVISI" }?.value]?.toBooleanStrictOrNull(),
                    DIGER_ILAC_KULLANIMI = answers[questions.find { it.key == "DIGER_ILAC_KULLANIMI" }?.value]?.toBooleanStrictOrNull(),
                    DIS_TEDAVISI = answers[questions.find { it.key == "DIS_TEDAVISI" }?.value]?.toBooleanStrictOrNull(),
                    ISHAL = answers[questions.find { it.key == "ISHAL" }?.value]?.toBooleanStrictOrNull(),
                    ASI_OLUNDU_MU = answers[questions.find { it.key == "ASI_OLUNDU_MU" }?.value]?.toBooleanStrictOrNull(),
                    KRONIK_HASTALIK = answers[questions.find { it.key == "KRONIK_HASTALIK" }?.value]?.toBooleanStrictOrNull(),
                    PARA_KARSILIGI_ILISKI = answers[questions.find { it.key == "PARA_KARSILIGI_ILISKI" }?.value]?.toBooleanStrictOrNull(),
                    FRENGI_GONORE = answers[questions.find { it.key == "FRENGI_GONORE" }?.value]?.toBooleanStrictOrNull(),
                    AIDS_HASTALIGI = answers[questions.find { it.key == "AIDS_HASTALIGI" }?.value]?.toBooleanStrictOrNull(),
                    AIDS_HASTASI_ILE_ILISKI = answers[questions.find { it.key == "AIDS_HASTASI_ILE_ILISKI" }?.value]?.toBooleanStrictOrNull(),
                    KAN_ALAN_KISI_ILE_ILISKI = answers[questions.find { it.key == "KAN_ALAN_KISI_ILE_ILISKI" }?.value]?.toBooleanStrictOrNull(),
                    UYUSTURUCU_KULLANIMI = answers[questions.find { it.key == "UYUSTURUCU_KULLANIMI" }?.value]?.toBooleanStrictOrNull(),
                    HORMON_ILAC_KULLANIMI = answers[questions.find { it.key == "HORMON_ILAC_KULLANIMI" }?.value]?.toBooleanStrictOrNull(),
                    AMELIYAT_ENDOSKOPI = answers[questions.find { it.key == "AMELIYAT_ENDOSKOPI" }?.value]?.toBooleanStrictOrNull(),
                    KALP_AKCIGER_HASTALIK = answers[questions.find { it.key == "KALP_AKCIGER_HASTALIK" }?.value]?.toBooleanStrictOrNull(),
                    NOBET_EPILEPSI_FELC = answers[questions.find { it.key == "NOBET_EPILEPSI_FELC" }?.value]?.toBooleanStrictOrNull(),
                    KANSER_TEDAVISI = answers[questions.find { it.key == "KANSER_TEDAVISI" }?.value]?.toBooleanStrictOrNull(),
                    SEKER_ROMATIZMA = answers[questions.find { it.key == "SEKER_ROMATIZMA" }?.value]?.toBooleanStrictOrNull(),
                    KAN_HASTALIGI = answers[questions.find { it.key == "KAN_HASTALIGI" }?.value]?.toBooleanStrictOrNull(),
                    SITMA_TUBERKULOZ = answers[questions.find { it.key == "SITMA_TUBERKULOZ" }?.value]?.toBooleanStrictOrNull(),
                    HEPATIT_TASIYICILIK = answers[questions.find { it.key == "HEPATIT_TASIYICILIK" }?.value]?.toBooleanStrictOrNull(),
                    HEPATITLI_ILISKI = answers[questions.find { it.key == "HEPATITLI_ILISKI" }?.value]?.toBooleanStrictOrNull(),
                    TOKSOPLAZMA = answers[questions.find { it.key == "TOKSOPLAZMA" }?.value]?.toBooleanStrictOrNull(),
                    BELIRLI_ULKELERDE_BULUNDUNUZ_MU = answers[questions.find { it.key == "BELIRLI_ULKELERDE_BULUNDUNUZ_MU" }?.value]?.toBooleanStrictOrNull(),
                    INGILTERE_KUZAY_IRLANDA = answers[questions.find { it.key == "INGILTERE_KUZAY_IRLANDA" }?.value]?.toBooleanStrictOrNull(),
                    DIGER_ULKELER = answers[questions.find { it.key == "DIGER_ULKELER" }?.value]?.toBooleanStrictOrNull(),
                    DELI_DANA_HASTALIGI = answers[questions.find { it.key == "DELI_DANA_HASTALIGI" }?.value]?.toBooleanStrictOrNull(),
                    BEYIN_ZARI_KORNEA_NAKLI = answers[questions.find { it.key == "BEYIN_ZARI_KORNEA_NAKLI" }?.value]?.toBooleanStrictOrNull(),
                    KAN_ORGAN_NAKLI = answers[questions.find { it.key == "KAN_ORGAN_NAKLI" }?.value]?.toBooleanStrictOrNull(),
                    BASKASININ_KANI_ILE_TEMAS = answers[questions.find { it.key == "BASKASININ_KANI_ILE_TEMAS" }?.value]?.toBooleanStrictOrNull(),
                    DOVME_ESTETIK_MUDAHALE = answers[questions.find { it.key == "DOVME_ESTETIK_MUDAHALE" }?.value]?.toBooleanStrictOrNull(),
                    KUDUZ_ASISI = answers[questions.find { it.key == "KUDUZ_ASISI" }?.value]?.toBooleanStrictOrNull(),
                    TUTUKLULUK = answers[questions.find { it.key == "TUTUKLULUK" }?.value]?.toBooleanStrictOrNull(),
                    KAN_BAGISI_ERKEK = answers[questions.find { it.key == "KAN_BAGISI_ERKEK" }?.value]?.toBooleanStrictOrNull(),
                    ERKEK_ERKEGE_ILISKI_KADIN_HAMILELIK = answers[questions.find { it.key == "ERKEK_ERKEGE_ILISKI_KADIN_HAMILELIK" }?.value]?.toBooleanStrictOrNull(),
                )
                val response = updateFormUseCase(request)
                if (response.success) {
                    _updateState.value = FormUpdateState.Success
                    setEditMode(false)
                    loadForm()
                } else {
                    _updateState.value = FormUpdateState.Error(response.resultMessage ?: "Form güncellenemedi.")
                }
            } catch (e: Exception) {
                _updateState.value = FormUpdateState.Error(e.message ?: "Form güncellenemedi.")
            }
        }
    }

    private fun parseFormDataToAnswers(formData: String?): Map<String, String> {
        return try {
            if (formData.isNullOrBlank()) return emptyMap()
            // Parse using org.json
            val jsonObject = JSONObject(formData)
            val result = mutableMapOf<String, String>()
            
            // Iterate through keys
            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val value = when (val jsonValue = jsonObject.opt(key)) {
                    is Boolean -> jsonValue.toString()
                    is Int, is Long, is Double -> jsonValue.toString()
                    is String -> jsonValue
                    JSONObject.NULL -> null
                    else -> {
                        try {
                            jsonValue.toString()
                        } catch (e: Exception) {
                            null
                        }
                    }
                }
                
                if (value != null) {
                    result[key] = value
                }
            }
            result
        } catch (jsonException: Exception) {
            try {
                val map = mutableMapOf<String, String>()
                val regex = """"(.*?)":\s*(true|false|\d+|".*?"|null)""".toRegex()
                regex.findAll(formData ?: "").forEach { match ->
                    val key = match.groupValues[1]
                    val value = match.groupValues[2].replace("\"", "")
                    if (value != "null") {
                        map[key] = value
                    }
                }
                return map
            } catch (e: Exception) {
                return emptyMap()
            }
        }
    }
}

data class FormViewState(
    val loading: Boolean = false,
    val error: String? = null,
    val form: com.ribuufing.bloodapp.feature.form.domain.response.GetFormResponse? = null,
    val questionsLoaded: Boolean = false
)

sealed class FormSubmitState {
    object Initial : FormSubmitState()
    object Loading : FormSubmitState()
    object Success : FormSubmitState()
    data class Error(val message: String) : FormSubmitState()
}

sealed class FormUpdateState {
    object Initial : FormUpdateState()
    object Loading : FormUpdateState()
    object Success : FormUpdateState()
    data class Error(val message: String) : FormUpdateState()
}