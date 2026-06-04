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
    private val _selectedTab = MutableStateFlow(1)
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

    // --- Sensei AI Assistant State (Official Chat, Voice & Memory Assistant) ---
    private var senseiRepository: com.example.architecture.database.SenseiRepository? = null

    private val _senseiChatHistory = MutableStateFlow<List<com.example.architecture.database.SenseiChatMessageEntity>>(emptyList())
    val senseiChatHistory: StateFlow<List<com.example.architecture.database.SenseiChatMessageEntity>> = _senseiChatHistory.asStateFlow()

    private val _playerMemory = MutableStateFlow<com.example.architecture.database.PlayerMemoryEntity?>(null)
    val playerMemory: StateFlow<com.example.architecture.database.PlayerMemoryEntity?> = _playerMemory.asStateFlow()

    private val _isSenseiThinking = MutableStateFlow(false)
    val isSenseiThinking: StateFlow<Boolean> = _isSenseiThinking.asStateFlow()

    // --- Real Academies Integration State Flows ---
    private val _realAcademies = MutableStateFlow<List<com.example.architecture.database.RealAcademyEntity>>(emptyList())
    val realAcademies: StateFlow<List<com.example.architecture.database.RealAcademyEntity>> = _realAcademies.asStateFlow()

    private val _realTournaments = MutableStateFlow<List<com.example.architecture.database.AcademyTournamentEntity>>(emptyList())
    val realTournaments: StateFlow<List<com.example.architecture.database.AcademyTournamentEntity>> = _realTournaments.asStateFlow()

    private val _selectedAcademyId = MutableStateFlow<Int?>(null)
    val selectedAcademyId: StateFlow<Int?> = _selectedAcademyId.asStateFlow()

    private val _selectedAcademyStudents = MutableStateFlow<List<com.example.architecture.database.GymStudentEntity>>(emptyList())
    val selectedAcademyStudents: StateFlow<List<com.example.architecture.database.GymStudentEntity>> = _selectedAcademyStudents.asStateFlow()

    fun initializeSensei(context: android.content.Context) {
        if (senseiRepository != null) return
        val db = com.example.architecture.database.SenseiRoomDatabase.getDatabase(context)
        val repo = com.example.architecture.database.SenseiRepository(db)
        senseiRepository = repo

        viewModelScope.launch {
            repo.chatHistory.collect { messages ->
                _senseiChatHistory.value = messages
            }
        }

        viewModelScope.launch {
            repo.playerMemory.collect { memory ->
                _playerMemory.value = memory ?: com.example.architecture.database.PlayerMemoryEntity()
            }
        }

        viewModelScope.launch {
            repo.allAcademies.collect { academies ->
                _realAcademies.value = academies
            }
        }

        viewModelScope.launch {
            repo.allTournaments.collect { tournaments ->
                _realTournaments.value = tournaments
            }
        }
        
        // Populate initial data if empty context is detected
        viewModelScope.launch {
            val direct = repo.getPlayerMemoryDirect()
            if (direct.completedQuestsCount == 0 && _senseiChatHistory.value.isEmpty()) {
                repo.savePlayerMemory(com.example.architecture.database.PlayerMemoryEntity())
                repo.addChatMessage("sensei", "Oss, jovem herói! Sou o Sensei AI, o guardião de sabedoria do JiuVerse. Como posso te guiar hoje em sua caminhada marcial?", false, "Iniciante")
            }

            // Populate default real-world academies on startup if empty
            delay(300)
            repo.allAcademies.collect { currentList ->
                if (currentList.isEmpty()) {
                    val defaultAcademies = listOf(
                        com.example.architecture.database.RealAcademyEntity(
                            name = "Alliance Itaim Bibi",
                            cnpj = "09.112.554/0001-30",
                            responsibleMaster = "Mestre Fabio Gurgel",
                            region = "São Paulo, SP",
                            phone = "(11) 97777-5544",
                            isVerified = true,
                            verificationDocUrl = "https://jus.br/cnpj/alliance-sp",
                            memberCount = 120,
                            jiuCoinsBalance = 52000,
                            realRankPoints = 940,
                            virtualRankPoints = 1250,
                            monetizationPlan = "Premium Dojo",
                            monetizationPrice = 199,
                            virtualGuildSynced = "Alliance Moema"
                        ),
                        com.example.architecture.database.RealAcademyEntity(
                            name = "Gracie Barra Rio Central",
                            cnpj = "14.285.961/0001-44",
                            responsibleMaster = "Mestre Carlos Gracie Jr.",
                            region = "Rio de Janeiro, RJ",
                            phone = "(21) 98888-7766",
                            isVerified = true,
                            verificationDocUrl = "https://jus.br/cnpj/gb-central",
                            memberCount = 180,
                            jiuCoinsBalance = 84000,
                            realRankPoints = 1120,
                            virtualRankPoints = 1540,
                            monetizationPlan = "Franquia Master",
                            monetizationPrice = 350,
                            virtualGuildSynced = "Gracie Barra central"
                        ),
                        com.example.architecture.database.RealAcademyEntity(
                            name = "Checkmat Pinheiros",
                            cnpj = "18.334.887/0001-90",
                            responsibleMaster = "Mestre Leo Vieira",
                            region = "São Paulo, SP",
                            phone = "(11) 96666-8899",
                            isVerified = false,
                            verificationDocUrl = "",
                            memberCount = 65,
                            jiuCoinsBalance = 24000,
                            realRankPoints = 510,
                            virtualRankPoints = 790,
                            monetizationPlan = "Plano Inicial",
                            monetizationPrice = 99,
                            virtualGuildSynced = "Atos San Diego"
                        )
                    )
                    defaultAcademies.forEach { repo.saveAcademy(it) }

                    // Default tournaments
                    repo.addTournament(
                        com.example.architecture.database.AcademyTournamentEntity(
                            academyId = 1,
                            title = "Desafio Estadual Meia Guarda Alliance",
                            eventType = "Campeonato Real",
                            entryFeeBrl = 80,
                            virtualSyncBonus = 400,
                            eventDate = "15 Junho 2026",
                            status = "Agendado"
                        )
                    )
                    repo.addTournament(
                        com.example.architecture.database.AcademyTournamentEntity(
                            academyId = 2,
                            title = "Copa Rio Sul Peso Absoluto",
                            eventType = "Campeonato Real",
                            entryFeeBrl = 120,
                            virtualSyncBonus = 800,
                            eventDate = "22 Junho 2026",
                            status = "Agendado"
                        )
                    )
                    repo.addTournament(
                        com.example.architecture.database.AcademyTournamentEntity(
                            academyId = 3,
                            title = "Seminário Passagem de Guarda Moderna",
                            eventType = "Seminário",
                            entryFeeBrl = 50,
                            virtualSyncBonus = 200,
                            eventDate = "28 Junho 2026",
                            status = "Agendado"
                        )
                    )

                    // Default students
                    repo.enrollStudent(com.example.architecture.database.GymStudentEntity(academyId = 1, name = "Bruno Malfacine", belt = "Preta", registrationApproved = true, virtualNickname = "MalfacinePassador"))
                    repo.enrollStudent(com.example.architecture.database.GymStudentEntity(academyId = 1, name = "Marcus Buchecha", belt = "Preta", registrationApproved = true, virtualNickname = "BuchechaUltra"))
                    repo.enrollStudent(com.example.architecture.database.GymStudentEntity(academyId = 2, name = "Guerreiro Copacabana", belt = "Azul", registrationApproved = false, virtualNickname = "GuerreiroCopa"))
                    repo.enrollStudent(com.example.architecture.database.GymStudentEntity(academyId = 2, name = "GuardaInvisivel", belt = "Roxa", registrationApproved = true, virtualNickname = "GuardaInvisivel"))
                    repo.enrollStudent(com.example.architecture.database.GymStudentEntity(academyId = 3, name = "Iniciante Pinheiros", belt = "Branca", registrationApproved = false, virtualNickname = "FaixaBranca99"))

                    // Automatically select first academy
                    selectAcademy(1)
                }
            }
        }
    }

    fun selectAcademy(id: Int?) {
        _selectedAcademyId.value = id
        if (id != null) {
            viewModelScope.launch {
                senseiRepository?.getStudentsForAcademy(id)?.collect { students ->
                    _selectedAcademyStudents.value = students
                }
            }
        } else {
            _selectedAcademyStudents.value = emptyList()
        }
    }

    fun registerAcademy(name: String, cnpj: String, master: String, region: String, phone: String, plan: String, price: Int) {
        viewModelScope.launch {
            val newGym = com.example.architecture.database.RealAcademyEntity(
                name = name,
                cnpj = cnpj,
                responsibleMaster = master,
                region = region,
                phone = phone,
                isVerified = false,
                verificationDocUrl = "",
                memberCount = 1,
                jiuCoinsBalance = 1500,
                realRankPoints = 200,
                virtualRankPoints = 350,
                monetizationPlan = plan,
                monetizationPrice = price,
                virtualGuildSynced = name + " Virtual"
            )
            senseiRepository?.saveAcademy(newGym)
        }
    }

    fun updateAcademy(academy: com.example.architecture.database.RealAcademyEntity) {
        viewModelScope.launch {
            senseiRepository?.updateAcademy(academy)
        }
    }

    fun verifyAcademy(academyId: Int, verified: Boolean) {
        viewModelScope.launch {
            val academy = _realAcademies.value.find { it.id == academyId }
            if (academy != null) {
                senseiRepository?.updateAcademy(academy.copy(isVerified = verified, verificationDocUrl = "https://jus.br/verification-approved-${academy.id}"))
            }
        }
    }

    fun addTournamentToAcademy(academyId: Int, title: String, type: String, fee: Int, bonus: Int, date: String) {
        viewModelScope.launch {
            val tournament = com.example.architecture.database.AcademyTournamentEntity(
                academyId = academyId,
                title = title,
                eventType = type,
                entryFeeBrl = fee,
                virtualSyncBonus = bonus,
                eventDate = date,
                status = "Agendado"
            )
            senseiRepository?.addTournament(tournament)
        }
    }

    fun deleteTournamentFromAcademy(eventId: Int) {
        viewModelScope.launch {
            senseiRepository?.deleteTournament(eventId)
        }
    }

    fun enrollStudentToAcademy(academyId: Int, name: String, belt: String, nickname: String, approved: Boolean = false) {
        viewModelScope.launch {
            val student = com.example.architecture.database.GymStudentEntity(
                academyId = academyId,
                name = name,
                belt = belt,
                registrationApproved = approved,
                virtualNickname = nickname
            )
            senseiRepository?.enrollStudent(student)
            
            // Increment member count in RealAcademy
            val academy = _realAcademies.value.find { it.id == academyId }
            if (academy != null) {
                senseiRepository?.updateAcademy(academy.copy(memberCount = academy.memberCount + 1))
            }
        }
    }

    fun approveStudent(studentId: Int, approved: Boolean) {
        viewModelScope.launch {
            senseiRepository?.verifyStudent(studentId, approved)
            
            // If approved, trigger collection updates
            val currentId = _selectedAcademyId.value
            if (currentId != null) {
                selectAcademy(currentId)
            }
        }
    }

    fun expelStudent(studentId: Int) {
        viewModelScope.launch {
            val student = _selectedAcademyStudents.value.find { it.studentId == studentId }
            if (student != null) {
                senseiRepository?.deleteStudent(studentId)
                
                // Decrement member count
                val academy = _realAcademies.value.find { it.id == student.academyId }
                if (academy != null) {
                    senseiRepository?.updateAcademy(academy.copy(memberCount = Math.max(0, academy.memberCount - 1)))
                }
                
                // Refresh list
                selectAcademy(student.academyId)
            }
        }
    }

    fun savePlayerMemory(memory: com.example.architecture.database.PlayerMemoryEntity) {
        viewModelScope.launch {
            senseiRepository?.savePlayerMemory(memory)
        }
    }

    fun clearSenseiChatHistory() {
        viewModelScope.launch {
            senseiRepository?.clearChatHistory()
            senseiRepository?.addChatMessage("sensei", "Memória limpa, herói! Vamos recomeçar do zero.", false, "Dojo")
        }
    }

    fun askSensei(prompt: String, speechTextOverride: ((String) -> Unit)? = null) {
        if (prompt.trim().isEmpty() || _isSenseiThinking.value) return

        _isSenseiThinking.value = true

        viewModelScope.launch {
            // First save user message to chat history
            senseiRepository?.addChatMessage("player", prompt, false, "Conversa")
            
            // Generate response
            val currentMemory = senseiRepository?.getPlayerMemoryDirect() ?: com.example.architecture.database.PlayerMemoryEntity()
            
            val configKey = BuildConfig.GEMINI_API_KEY
            val isMockOffline = configKey.isEmpty() || configKey == "MY_GEMINI_API_KEY"

            val systemSetupPrompt = """
                Você é o **Sensei AI**, o assistente oficial inteligente e sábio de **JiuVerse** (um MMORPG de artes marciais e jiu-jitsu social de alta concorrência).
                Sua personalidade é carismática, mística, firme e encorajadora. Você usa termos do jiu-jitsu como "Oss", "Tatame", "Guardeiro", "Passador", "Alavanca" e fala um português heróico e inspirador.
                
                Você conhece o estado atual do jogador (PLAYER MEMORY):
                - Nome: ${currentMemory.playerName}
                - Faixa: ${currentMemory.playerBelt}
                - Estilo Preferido: ${currentMemory.favoriteStyle}
                - Missões Cumpridas: ${currentMemory.completedQuestsCount}
                - Último Local: ${currentMemory.lastVisitedRegion}
                - Minutos Treinados: ${currentMemory.totalTrainingMinutes}
                - Reputação: ${currentMemory.masterReputation}
                
                O usuário está te abordando. Responda em português de forma concisa (máximo 4-5 frases para ser amigável em chat de voz e texto no celular).
                Forneça respostas específicas de acordo com o que ele perguntou:
                1. Guia para iniciantes: Ofereça conselhos de postura, quedas e guarda fechada.
                2. Sugestão de missões: Proponha missões como treinar na areia do Arpoador, desafiar o Bot de Sparring, ou meditar na Baía.
                3. Ajuda no mapa: Explique como encontrar o Gracie Barra Central, Codan Shugyo ou Tiger Muay Thai.
                4. Ajuda em eventos: Fale sobre a abertura do Grand Prix de Peso Absoluto ou copas síncronas.
                
                Lembre-se: Use a memória dele para dar respostas incrivelmente contextuais!
            """.trimIndent()

            val aiAnswerText = try {
                if (isMockOffline) {
                    delay(1200)
                    generateLocalSenseiReply(prompt, currentMemory)
                } else {
                    val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$configKey"
                    val requestJson = JSONObject()
                    
                    val partsJson = JSONObject().put("text", prompt)
                    val contentJson = JSONObject().put("parts", JSONArray().put(partsJson))
                    requestJson.put("contents", JSONArray().put(contentJson))

                    val sysPart = JSONObject().put("text", systemSetupPrompt)
                    val sysContent = JSONObject().put("parts", JSONArray().put(sysPart))
                    requestJson.put("systemInstruction", sysContent)

                    val genConfig = JSONObject()
                    genConfig.put("temperature", 0.8)
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
                        partsArr.getJSONObject(0).getString("text")
                    } else {
                        "Oss, ${currentMemory.playerName}! Meus canais de energia estelar estão instáveis no momento (HTTP ${response.code}). Como conselho de mestre: ${generateLocalSenseiReply(prompt, currentMemory)}"
                    }
                }
            } catch (e: Exception) {
                "Houve uma interferência no éter: ${e.localizedMessage}. Mas ouça meu conselho interno: ${generateLocalSenseiReply(prompt, currentMemory)}"
            }

            // Save response to db
            senseiRepository?.addChatMessage("sensei", aiAnswerText, true, "Conversa")
            
            // Speak response if helper is connected
            speechTextOverride?.invoke(aiAnswerText)

            _isSenseiThinking.value = false
        }
    }

    private fun generateLocalSenseiReply(prompt: String, memory: com.example.architecture.database.PlayerMemoryEntity): String {
        val q = prompt.uppercase()
        val name = memory.playerName
        val belt = memory.playerBelt
        
        return when {
            q.contains("INICIANTE") || q.contains("GUIDE") || q.contains("COMEÇAR") || q.contains("COMEÇO") || q.contains("DICA") -> {
                "Oss! Grande herói $name! Como você treina com a $belt, meu conselho inicial é focar na postura defensiva. Se sua guarda for transpassada, use as alavancas do quadril. Vá ao Gracie Barra Central treinar a Passagem Invisível com o Grande Mestre Rickson!"
            }
            q.contains("MISSÃO") || q.contains("MISSAO") || q.contains("QUEST") || q.contains("TAREFA") -> {
                val newCount = memory.completedQuestsCount + 1
                viewModelScope.launch {
                    senseiRepository?.savePlayerMemory(memory.copy(completedQuestsCount = newCount, masterReputation = memory.masterReputation + 15))
                }
                "Para sua caminhada marcial, $name, sugiro a missão: 'Limpeza de Tatame Cósmica'. Complete 5 sparrings na areia molhada da praia e retorne para receber +15 de Reputação. Acabo de registrar o início desta jornada em sua memória!"
            }
            q.contains("MAPA") || q.contains("LOCAL") || q.contains("ONDE") || q.contains("ACADEMIA") || q.contains("DOJO") -> {
                "No mapa do JiuVerse, $name, recomendo que visite as três coordenadas sagradas: 1. Gracie Barra Central na Baía para lutas NoGi; 2. Kodan Shugyo Academy na colina para treinar quedas circulares; 3. Tiger Muay Thai RJ ao lado para refinar os socos energéticos."
            }
            q.contains("EVENTO") || q.contains("TORNEIO") || q.contains("COPA") || q.contains("PROVA") -> {
                "Oss, herói! No momento os administradores estão rodando a Copa Angra Síncrona, assistida via Streaming Tab! E as inscrições para o lendário Grand Prix de Peso Absoluto estão ativas. O lutador que mantiver o foco e a respiração no dojo levará o cinturão!"
            }
            else -> {
                "Oss! Te escuto perfeitamente, nobre $name, portador da nossa $belt. No JiuVerse, a mente do guerreiro deve ser maleável como a água e inquebrável como o carboneto de silício. Qual segredo das alavancas você quer decifrar comigo hoje?"
            }
        }
    }
}
