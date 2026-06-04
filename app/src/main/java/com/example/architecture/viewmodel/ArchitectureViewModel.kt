package com.example.architecture.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.architecture.model.BlueprintData
import com.example.architecture.model.ChatMessage
import com.example.architecture.model.FolderItem
import com.example.architecture.model.PlaybookIncident
import com.example.architecture.model.PrismaModel
import com.example.architecture.model.RoadmapPhase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class ArchitectureViewModel : ViewModel() {

    // --- Tab Navigation State ---
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    // --- Interactive Schema State ---
    private val _selectedModel = MutableStateFlow<PrismaModel>(BlueprintData.prismaModels.first())
    val selectedModel: StateFlow<PrismaModel> = _selectedModel.asStateFlow()

    fun selectModel(model: PrismaModel) {
        _selectedModel.value = model
    }

    // --- Directory Tree State ---
    private val _expandedFolders = MutableStateFlow<Set<String>>(setOf("jiuverse-root"))
    val expandedFolders: StateFlow<Set<String>> = _expandedFolders.asStateFlow()

    private val _selectedFile = MutableStateFlow<FolderItem?>(null)
    val selectedFile: StateFlow<FolderItem?> = _selectedFile.asStateFlow()

    fun toggleFolder(folderPath: String) {
        val currentSet = _expandedFolders.value.toMutableSet()
        if (currentSet.contains(folderPath)) {
            currentSet.remove(folderPath)
        } else {
            currentSet.add(folderPath)
        }
        _expandedFolders.value = currentSet
    }

    fun selectFile(file: FolderItem) {
        if (file.isFile) {
            _selectedFile.value = file
        }
    }

    // --- Scalability Simulator Inputs ---
    private val _ccuInput = MutableStateFlow(100000) // 1k to 150k
    val ccuInput: StateFlow<Int> = _ccuInput.asStateFlow()

    private val _tickRateInput = MutableStateFlow(10) // 5Hz to 30Hz
    val tickRateInput: StateFlow<Int> = _tickRateInput.asStateFlow()

    private val _voipActivePercent = MutableStateFlow(25) // 0% to 100% of CCU in voice
    val voipActivePercent: StateFlow<Int> = _voipActivePercent.asStateFlow()

    fun updateCCU(value: Int) { _ccuInput.value = value }
    fun updateTickRate(value: Int) { _tickRateInput.value = value }
    fun updateVoipPercent(value: Int) { _voipActivePercent.value = value }

    // --- Sandbox Incident Simulator ---
    private val _runningIncidentId = MutableStateFlow<String?>(null)
    val runningIncidentId: StateFlow<String?> = _runningIncidentId.asStateFlow()

    private val _currentLogs = MutableStateFlow<List<String>>(emptyList())
    val currentLogs: StateFlow<List<String>> = _currentLogs.asStateFlow()

    fun runIncidentSimulation(incident: PlaybookIncident) {
        if (_runningIncidentId.value != null) return // Already running
        _runningIncidentId.value = incident.id
        _currentLogs.value = emptyList()

        viewModelScope.launch {
            incident.simulationLogs.forEachIndexed { index, log ->
                val timeStamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
                _currentLogs.value = _currentLogs.value + "[$timeStamp] $log"
                delay(1200) // Stagger logs for cool realism
            }
            _runningIncidentId.value = null
        }
    }

    // --- Interactive Roadmap Phase ---
    private val _selectedPhase = MutableStateFlow<RoadmapPhase>(BlueprintData.roadmapPhases.first())
    val selectedPhase: StateFlow<RoadmapPhase> = _selectedPhase.asStateFlow()

    fun selectPhase(phase: RoadmapPhase) {
        _selectedPhase.value = phase
    }

    // --- AI Chat Assistant (Arquiteto Coach) State ---
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = "ai",
                content = "Olá, meu caro colega e engenheiro! Sou o Arquiteto de Software Sênior do JiuVerse. Projetar um MMORPG social de alta concorrência como o JiuVerse é uma jornada fascinante. Me pergunte qualquer coisa sobre a infraestrutura, WebRTC, conexões Socket.IO redundantes, mitigação de exploits ou modelagem do Prisma!",
                timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    fun askArchitectAI(prompt: String) {
        if (prompt.trim().isEmpty() || _isChatLoading.value) return

        val userMsgTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val userMsg = ChatMessage("user", prompt, userMsgTime)
        _chatMessages.value = _chatMessages.value + userMsg

        _isChatLoading.value = true

        viewModelScope.launch(Dispatchers.IO) {
            val systemInstructionsPrompt = """
                Você é um Arquiteto de Software Sênior especializado em arquiteturas escaláveis de MMORPGs sociais populares (tais como Habbo Hotel, Club Penguin e Roblox).
                Você está orientando e aprovando o desenvolvimento do JiuVerse, um jogo de jiu-jitsu online com microserviços.
                Atualmente, a pilha de tecnologia aprovada é:
                Frontend: React Native, Expo, TypeScript.
                Backend: NestJS, Prisma, PostgreSQL, Redis Cluster, Socket.IO.
                Comunicações: WebRTC, servidores TURN (Coturn).
                Configuração infra: Docker, PM2, Nginx Sticky Session de proxy reverso e Cloudflare.
                
                Responda com seriedade técnica, de forma clara, amigável e objetiva, focando em responder especificações do ecossistema do JiuVerse em português.
                Sempre que aplicável, propose trechos rápidos de código (TypeScript, Prisma, ou JS) e de desenho de infra de alto nível.
                Foque nas dúvidas do usuário para enriquecer exponencialmente o blueprint dele.
            """.trimIndent()

            try {
                val configKey = BuildConfig.GEMINI_API_KEY
                if (configKey.isEmpty() || configKey == "MY_GEMINI_API_KEY") {
                    // Pre-packaged high fidelity dynamic answers if API Key is not configured
                    delay(1500)
                    val mockResponse = getLocalBlueprintResponse(prompt)
                    val aiMsgTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                    _chatMessages.value = _chatMessages.value + ChatMessage("ai", mockResponse, aiMsgTime)
                } else {
                    val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$configKey"

                    // Structure system instructions and contents using standard org.json
                    val requestJson = JSONObject()
                    
                    val partsJson = JSONObject().put("text", prompt)
                    val contentJson = JSONObject().put("parts", JSONArray().put(partsJson))
                    requestJson.put("contents", JSONArray().put(contentJson))

                    val sysPart = JSONObject().put("text", systemInstructionsPrompt)
                    val sysContent = JSONObject().put("parts", JSONArray().put(sysPart))
                    requestJson.put("systemInstruction", sysContent)

                    // Generation config
                    val genConfig = JSONObject()
                    genConfig.put("temperature", 0.7)
                    requestJson.put("generationConfig", genConfig)

                    val requestBodyMsg = requestJson.toString().toRequestBody("application/json".toMediaType())
                    
                    val request = Request.Builder()
                        .url(url)
                        .post(requestBodyMsg)
                        .build()

                    val response = httpClient.newCall(request).execute()
                    if (response.isSuccessful) {
                        val responseBodyStr = response.body?.string() ?: ""
                        val responseJson = JSONObject(responseBodyStr)
                        
                        val candidates = responseJson.getJSONArray("candidates")
                        val firstCandidate = candidates.getJSONObject(0)
                        val contentObject = firstCandidate.getJSONObject("content")
                        val partsArr = contentObject.getJSONArray("parts")
                        val textAnswer = partsArr.getJSONObject(0).getString("text")

                        val aiMsgTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                        _chatMessages.value = _chatMessages.value + ChatMessage("ai", textAnswer, aiMsgTime)
                    } else {
                        val errorMsg = "Serviço temporariamente indisponível (HTTP ${response.code}). Detalhe: ${response.message}. Como arquiteto, sugiro configurar a chave de segredos no AI Studio para respostas sob demanda completas."
                        val aiMsgTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                        _chatMessages.value = _chatMessages.value + ChatMessage("ai", errorMsg, aiMsgTime)
                    }
                }
            } catch (e: Exception) {
                val errorMsg = "Erro na conexão com o servidor de IA: ${e.message}. Para um conselho rápido offline:\n\n${getLocalBlueprintResponse(prompt)}"
                val aiMsgTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                _chatMessages.value = _chatMessages.value + ChatMessage("ai", errorMsg, aiMsgTime)
            } finally {
                _isChatLoading.value = false
            }
        }
    }

    private fun getLocalBlueprintResponse(prompt: String): String {
        val q = prompt.uppercase()
        return when {
            q.contains("WEBRTC") || q.contains("VOZ") || q.contains("COTURN") || q.contains("TURN") -> {
                """
                    Entendido! Como Arquiteto, vamos nos focar em como WebRTC escalará para áudio por proximidade no JiuVerse:
                    
                    1. **Porque WebRTC?** Queremos áudio de ultra-baixa latência (<200ms) direto P2P quando viável. 
                    2. **Limitações do Mesh:** Se 10 avatares treinam no mesmo tatame, uma topologia Mesh criará 10*(10-1) = 90 streams de áudio ativas! Isso esgota a bateria de smartphones mais modestos.
                    3. **Solução Híbrida (SFU/MCU):** 
                       - Salas privadas pequenas (<10 pessoas) operam em topologia Mesh P2P pura via canais SDP para salvar custos de infraestrutura.
                       - Quartos públicos grandes (Ex: Dojo Central ou Academias Premium com 50+ pessoas) usam conexão única a um **SFU** (como LiveKit ou Mediasoup que rodam em NodeJS/Rust). O SFU reduz a multiplexação: cada avatar envia 1 stream de áudio e recebe apenas as streams das pessoas mais próximas, calculadas pelo coordenador de salas!
                    
                    Deseja que eu escreva um template de sinalização simples para você colocar no NestJS?
                """.trimIndent()
            }
            q.contains("REDIS") || q.contains("PUB/SUB") || q.contains("PUB") -> {
                """
                    O uso do Redis é a pedra angular para escalar o JiuVerse para 100k usuários concorrentes! No NestJS, faremos o seguinte:
                    
                    1. **WebSockets Sem Estado (Stateless):** Para suportar 100.000 pessoas, precisaremos de uns 10 pods de servidores WebSockets Socket.IO gerenciados no Kubernetes/Docker. No entanto, se o Jogador A está conectado no 'Servidor 1' e o Jogador B está no 'Servidor 2', eles precisam conversar.
                    2. **Redis Adapter:** Configuramos o `@socket.io/redis-adapter` em cada réplica NestJS. Todas as mensagens de movimentações ou chat enviadas a salas do Socket.IO serão publicadas no canal global de pub/sub do Redis e distribuídas a todos os outros pods instantaneamente!
                    3. **Valores Voláteis no Redis:** Coordenadas de players são salvas com expiração rápida no cache Redis (`SETEX player_pos_1 300 x_y_str`), reduzindo gravações de IO do PostgreSQL a quase zero!
                """.trimIndent()
            }
            q.contains("DUPLICAR") || q.contains("MARKETPLACE") || q.contains("EXPLOIT") || q.contains("CHEAT") -> {
                """
                    Fraude na economia e duplicação de itens são ameaças fatais para jogos baseados em Habbo ou MMOs com comércio. Veja como resolver com a nossa arquitetura:
                    
                    1. **Locks Pessimistas no Prisma:** Em uma transação de troca (Trade) ou compra no Marketplace, nunca faça apenas leituras normais seguidas de escritas, pois um clique duplo milimétrico causará condições de corrida (Race Conditions). 
                    Você deve rodar transações em isolamento de banco SERIALIZABLE ou executar raw queries como `SELECT * FROM "InventoryItem" WHERE id = 'uuid' FOR UPDATE`.
                    2. **Controle Estrito de Sessão no Backend:** O cliente (React Native) nunca dita se ele 'perdeu' ou 'ganhou' um item. O backend valida via Prisma a posse real consultando a tabela `InventoryItem` antes de confirmar transações de compra globais.
                """.trimIndent()
            }
            else -> {
                """
                    Ótima pergunta! Para o ecossistema do JiuVerse, é vital compreender que:
                    - **O Servidor é Autoritativo:** Todos os cálculos de alcance de chat de voz por coordenadas tridimensionais, bloqueios anti-movimento adulterado e moderação de contas ocorrem inteiramente de forma centralizada no backend NestJS e são armazenados no cache sincronizado do Redis.
                    - **O Frontend Expo é um Espelho Visivo:** O React Native com TypeScript consome eventos Socket.IO na taxa de atualização aproximada de 10Hz a 12Hz, garantindo que o dispositivo não sofra de throttles de renderização enquanto garante fluidez por meio de algoritmos de interpolação linear (LERP) das coordenadas recebidas de avatares próximos.
                    
                    O que mais eu posso elucidar sobre a arquitetura para você? Pergunte sobre Docker, Prisma relations, anti-spam ou banco de dados!
                """.trimIndent()
            }
        }
    }
}
