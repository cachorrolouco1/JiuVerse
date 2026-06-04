package com.example.architecture.model

object BlueprintData {

    val prismaModels = listOf(
        PrismaModel(
            name = "User",
            purpose = "Tabela principal de credenciais e controle de acessos da conta dos usuários.",
            fields = listOf(
                PrismaField("id", "String (UUID)", isId = true, description = "Chave primária única."),
                PrismaField("email", "String", description = "Endereço de e-mail único para login."),
                PrismaField("passwordHash", "String", description = "Hash Argon2 seguro da senha do usuário."),
                PrismaField("status", "Enum (ACTIVE, MUTED, BANNED)", description = "Estado de moderação da conta."),
                PrismaField("role", "Enum (USER, COMPANION, ADMIN, MODERATOR)", description = "Permissões de acesso."),
                PrismaField("createdAt", "DateTime", description = "Data de criação da conta."),
                PrismaField("updatedAt", "DateTime", description = "Data da última modificação."),
                PrismaField("character", "Character?", isRelation = true, description = "Relacionamento um-para-um com o avatar no jogo.")
            ),
            relations = listOf("Character: Relacionamento 1:1"),
            rawCode = """model User {
  id           String    @id @default(uuid())
  email        String    @unique
  passwordHash String
  status       AccountStatus @default(ACTIVE)
  role         UserRole      @default(USER)
  createdAt    DateTime  @default(now())
  updatedAt    DateTime  @updatedAt
  character    Character?
}

enum AccountStatus {
  ACTIVE
  MUTED
  BANNED
}

enum UserRole {
  USER
  COMPANION
  ADMIN
  MODERATOR
}"""
        ),
        PrismaModel(
            name = "Character",
            purpose = "Armazena a entidade do jogador logado (avatar), seu status atual e carteira.",
            fields = listOf(
                PrismaField("id", "String (UUID)", isId = true, description = "Identificador único do personagem."),
                PrismaField("userId", "String", isRelation = true, description = "FK apontando para User."),
                PrismaField("nickname", "String", description = "Nome público exibido no avatar (único)."),
                PrismaField("xp", "Int", description = "Experiência acumulada total."),
                PrismaField("level", "Int", description = "Nível atual de Jiu-Jitsu do personagem (Faixas)."),
                PrismaField("coins", "Int", description = "Moeda virtual JiuCoins (carteira secundária)."),
                PrismaField("currentRoomId", "String?", description = "ID da sala pública ou privada em que o avatar se encontra."),
                PrismaField("posX", "Float", description = "Coordenada X do avatar para spawn."),
                PrismaField("posY", "Float", description = "Coordenada Y do avatar para spawn."),
                PrismaField("inventory", "InventoryItem[]", isRelation = true, description = "Lista de itens possuídos no inventário."),
                PrismaField("gyms", "Gym[]", isRelation = true, description = "Academias criadas pelo próprio personagem.")
            ),
            relations = listOf("User: Relacionamento 1:1", "InventoryItem: Relacionamento 1:N", "Gym: Relacionamento 1:N"),
            rawCode = """model Character {
  id            String          @id @default(uuid())
  userId        String          @unique
  user          User            @relation(fields: [userId], references: [id], onDelete: Cascade)
  nickname      String          @unique
  xp            Int             @default(0)
  level         Int             @default(1)
  coins         Int             @default(500)
  currentRoomId String?
  posX          Float           @default(0.0)
  posY          Float           @default(0.0)
  inventory     InventoryItem[]
  gyms          Gym[]
  createdAt     DateTime        @default(now())
  updatedAt     DateTime        @updatedAt
}"""
        ),
        PrismaModel(
            name = "Gym",
            purpose = "Representa as academias (quartos e dojos customizáveis) que os jogadores criam e decoram.",
            fields = listOf(
                PrismaField("id", "String (UUID)", isId = true, description = "ID único da academia pública/privada."),
                PrismaField("ownerId", "String", isRelation = true, description = "FK apontando para Character (Criador)."),
                PrismaField("name", "String", description = "Nome da academia."),
                PrismaField("layoutGrid", "String", description = "JSON contendo a matriz de blocos 3D/isométricos colocados no dojo."),
                PrismaField("reputation", "Int", description = "Fama da academia decorada baseado em votos."),
                PrismaField("maxCapacity", "Int", description = "Limite máximo de jogadores simultâneos no dojo."),
                PrismaField("isPublic", "Boolean", description = "Indica se aparece na listagem pública do mapa.")
            ),
            relations = listOf("Character: Relacionamento N:1 (Dono)"),
            rawCode = """model Gym {
  id          String    @id @default(uuid())
  ownerId     String
  owner       Character @relation(fields: [ownerId], references: [id], onDelete: Cascade)
  name        String
  layoutGrid  String    // Armazena JSON de tiles, tatames e acessórios
  reputation  Int       @default(0)
  maxCapacity Int       @default(50)
  isPublic    Boolean   @default(true)
  createdAt   DateTime  @default(now())
}"""
        ),
        PrismaModel(
            name = "InventoryItem",
            purpose = "Item específico associado ao inventário de um personagem específico.",
            fields = listOf(
                PrismaField("id", "String (UUID)", isId = true, description = "ID específico desta instância de item."),
                PrismaField("characterId", "String", isRelation = true, description = "FK que correlaciona o item ao personagem."),
                PrismaField("templateId", "String", isRelation = true, description = "FK apontar o item para seu design/ID geral."),
                PrismaField("equipped", "Boolean", description = "Se o item está ativamente vestido no avatar."),
                PrismaField("placedInGymId", "String?", description = "ID da academia caso seja um móvel inserido em grid.")
            ),
            relations = listOf("Character: Relacionamento N:1", "ItemTemplate: Relacionamento N:1"),
            rawCode = """model InventoryItem {
  id         String       @id @default(uuid())
  characterId String
  character  Character    @relation(fields: [characterId], references: [id], onDelete: Cascade)
  templateId String
  template   ItemTemplate @relation(fields: [templateId], references: [id])
  equipped   Boolean      @default(false)
  placedInGymId String?   // Mobiliário posicionado
  acquiredAt DateTime     @default(now())
}"""
        ),
        PrismaModel(
            name = "ItemTemplate",
            purpose = "Catálogo geral de roupas, faixas, cosméticos do avatar e itens decorativos de tatames do dojo.",
            fields = listOf(
                PrismaField("id", "String (UUID)", isId = true, description = "ID único do item geral."),
                PrismaField("name", "String", description = "Nome amigável do item (Ex: Kimono Clássico)."),
                PrismaField("type", "Enum (WEARABLE, FURNITURE, BADGE)", description = "Tipo do item para renderização."),
                PrismaField("price", "Int", description = "Preço de venda na loja global."),
                PrismaField("spriteMetadata", "String", description = "Informações JSON de spritesheet ou assets para o app."),
                PrismaField("isTradable", "Boolean", description = "Se o item pode ser negociado no marketplace.")
            ),
            relations = listOf("InventoryItem: Relacionamento 1:N"),
            rawCode = """model ItemTemplate {
  id             String          @id @default(uuid())
  name           String
  type           ItemType
  price          Int             @default(100)
  spriteMetadata String          // JSON de sprite do motor 2.5D
  isTradable     Boolean         @default(true)
  inventoryItems InventoryItem[]
}

enum ItemType {
  WEARABLE
  FURNITURE
  BADGE
}"""
        ),
        PrismaModel(
            name = "MarketplaceListing",
            purpose = "Controla o marketplace entre jogadores P2P com compra segura e auditoria.",
            fields = listOf(
                PrismaField("id", "String (UUID)", isId = true, description = "ID do anúncio de venda."),
                PrismaField("sellerId", "String", isRelation = true, description = "FK do anunciante."),
                PrismaField("itemId", "String", isRelation = true, description = "FK do InventoryItem posto à venda."),
                PrismaField("price", "Int", description = "Valor cobrado em JiuCoins."),
                PrismaField("status", "Enum (ACTIVE, SOLD, CANCELLED)", description = "Status do anúncio."),
                PrismaField("createdAt", "DateTime", description = "Data da publicação.")
            ),
            relations = listOf("Character: Relacionamento N:1 (Vendedor)", "InventoryItem: Relacionamento 1:1"),
            rawCode = """model MarketplaceListing {
  id         String      @id @default(uuid())
  sellerId   String
  itemId     String      @unique
  item       InventoryItem @relation(fields: [itemId], references: [id], onDelete: Cascade)
  price      Int
  status     ListingStatus @default(ACTIVE)
  createdAt  DateTime    @default(now())
}

enum ListingStatus {
  ACTIVE
  SOLD
  CANCELLED
}"""
        ),
        PrismaModel(
            name = "Academy",
            purpose = "Modelo central do sistema de equipes. Suporta insígnias, fundos econômicos e gerenciamento de permissões.",
            fields = listOf(
                PrismaField("id", "String (UUID)", isId = true, description = "ID único da academia no JiuVerse."),
                PrismaField("name", "String", description = "Nome exclusivo da academia (Ex: Alliance Moema)."),
                PrismaField("logoUrl", "String", description = "Identificador do ícone customizado de brasão."),
                PrismaField("description", "String", description = "Biografia descritiva e metas de convocação."),
                PrismaField("ownerId", "String", isRelation = true, description = "Chave estrangeira apontando para o lutador criador/dono."),
                PrismaField("treasuryBalance", "Int", description = "Saldo em moedas própria JiuTokens (sub-economia local)."),
                PrismaField("tokenSymbol", "String", description = "Símbolo curto do token exclusivo (Ex: GRAC, ALLN)."),
                PrismaField("tokenTaxRate", "Float", description = "Porcentagem retida de recompensas do atleta para o fundo da equipe (0% a 50%)."),
                PrismaField("reputation", "Int", description = "Soma acumulada de prestígio e eventos vencidos."),
                PrismaField("level", "Int", description = "Nível de expansão da academia (desbloqueia maiores capacidades).")
            ),
            relations = listOf("Character: Relacionamento N:1 (Dono)", "AcademyMember: Relacionamento 1:N", "AcademyEvent: Relacionamento 1:N"),
            rawCode = """model Academy {
  id              String          @id @default(uuid())
  name            String          @unique
  logoUrl         String          @default("shield_01")
  description     String
  ownerId         String
  owner           Character       @relation(fields: [ownerId], references: [id])
  treasuryBalance Int             @default(1000) // Saldo inicial para prover recrutamentos
  tokenSymbol     String          @default("OSS") // Nome do Token da Academia
  tokenTaxRate    Float           @default(10.0)  // Retenção percentual
  reputation      Int             @default(0)
  level           Int             @default(1)
  members         AcademyMember[]
  events          AcademyEvent[]
  transactions    AcademyTransaction[]
  createdAt       DateTime        @default(now())
  updatedAt       DateTime        @updatedAt
}"""
        ),
        PrismaModel(
            name = "AcademyMember",
            purpose = "Controla recrutamento, promoções de cargos (Líder, Instrutor, Aluno) e relatórios de comissões.",
            fields = listOf(
                PrismaField("id", "String (UUID)", isId = true, description = "ID da filiação associada."),
                PrismaField("academyId", "String", isRelation = true, description = "FK apontando para a qual academia pertence."),
                PrismaField("characterId", "String", isRelation = true, description = "FK apontando para o jogador cadastrado."),
                PrismaField("role", "Enum (OWNER, MASTER, INSTRUCTOR, ATHLETE)", description = "Permissões e divisão do estatuto de cargos."),
                PrismaField("status", "Enum (INVITED, APPLIED, ACTIVE)", description = "Estado do fluxo de recrutamento/aceitação."),
                PrismaField("earnedTokens", "Int", description = "Relatório cumulativo de comissões ganhas em treinos."),
                PrismaField("joinedAt", "DateTime", description = "Data da consolidação no time.")
            ),
            relations = listOf("Academy: Relacionamento N:1", "Character: Relacionamento 1:1"),
            rawCode = """model AcademyMember {
  id          String      @id @default(uuid())
  academyId   String
  academy     Academy     @relation(fields: [academyId], references: [id], onDelete: Cascade)
  characterId String      @unique
  character   Character   @relation(fields: [characterId], references: [id], onDelete: Cascade)
  role        MemberRole  @default(ATHLETE)
  status      JoinStatus  @default(INVITED)
  earnedTokens Int        @default(0)
  joinedAt    DateTime    @default(now())
}

enum MemberRole {
  OWNER
  MASTER
  INSTRUCTOR
  ATHLETE
}

enum JoinStatus {
  INVITED
  APPLIED
  ACTIVE
}"""
        ),
        PrismaModel(
            name = "AcademyEvent",
            purpose = "Representa eventos organizados pelas academias, regulando os custos e premiações em tokens.",
            fields = listOf(
                PrismaField("id", "String (UUID)", isId = true, description = "ID único do evento agendado."),
                PrismaField("academyId", "String", isRelation = true, description = "FK associando o evento à academia organizadora."),
                PrismaField("name", "String", description = "Título do evento (Ex: Copa Interna Gracie)."),
                PrismaField("type", "Enum (SEMINAR, TOURNAMENT, GRADUATION, CAMP)", description = "Tipo do encontro com regras específicas."),
                PrismaField("description", "String", description = "Ementa explicativa e agenda de lutas."),
                PrismaField("scheduledAt", "DateTime", description = "Data e hora programada."),
                PrismaField("durationMinutes", "Int", description = "Duração estimada do seminário ou lutas."),
                PrismaField("tokenCost", "Int", description = "Taxa de inscrição cobrada em tokens locais da equipe."),
                PrismaField("payoutPool", "Int", description = "Fundo de moedas distribuído aos melhores rankeados do evento.")
            ),
            relations = listOf("Academy: Relacionamento N:1"),
            rawCode = """model AcademyEvent {
  id              String      @id @default(uuid())
  academyId       String
  academy         Academy     @relation(fields: [academyId], references: [id], onDelete: Cascade)
  name            String
  type            EventType   @default(SPARRING_CAMP)
  description     String
  scheduledAt     DateTime
  durationMinutes Int         @default(60)
  tokenCost       Int         @default(50) // Custo de inscrição
  payoutPool      Int         @default(500) // Prêmio
  createdAt       DateTime    @default(now())
}

enum EventType {
  SEMINAR
  TOURNAMENT
  GRADUATION
  SPARRING_CAMP
}"""
        ),
        PrismaModel(
            name = "AcademyTransaction",
            purpose = "Livro-caixa e histórico detalhado das movimentações de JiuTokens na corporação.",
            fields = listOf(
                PrismaField("id", "String (UUID)", isId = true, description = "ID único da movimentação auditada."),
                PrismaField("academyId", "String", isRelation = true, description = "FK à qual tesouraria foi afetada."),
                PrismaField("characterId", "String?", isRelation = true, description = "FK opcional do lutador associado à entrada/saída."),
                PrismaField("amount", "Int", description = "Quantidade de tokens transacionada (pode ser negativa para saídas)."),
                PrismaField("type", "Enum (MEMBER_TAX, EVENT_FEE, REWRITE_BONUS, SYSTEM_MINT)", description = "Classificação para relatórios contábeis de faturamento."),
                PrismaField("notes", "String", description = "Justificativa complementar para auditoria contra lavagem ou bots.")
            ),
            relations = listOf("Academy: Relacionamento N:1", "Character: Relacionamento N:1"),
            rawCode = """model AcademyTransaction {
  id          String          @id @default(uuid())
  academyId   String
  academy     Academy         @relation(fields: [academyId], references: [id], onDelete: Cascade)
  characterId String?
  character   Character?      @relation(fields: [characterId], references: [id])
  amount      Int
  type        TxType
  notes       String
  createdAt   DateTime        @default(now())
}

enum TxType {
  MEMBER_TAX
  EVENT_FEE
  REWRITE_BONUS
  SYSTEM_MINT
  PAYOUT_REWARD
}"""
        )
    )

    val folderTree = FolderItem(
        name = "jiuverse-root",
        description = "Diretório principal da arquitetura do ecossistema do JiuVerse.",
        isFile = false,
        children = listOf(
            FolderItem(
                name = "frontend-expo",
                description = "Aplicação móvel e Web construída com React Native, Expo e TypeScript.",
                isFile = false,
                children = listOf(
                    FolderItem("package.json", "Dependências geridas pelo Expo (com expo-webrtc, socket.io-client, react-native, lucide-react-native).", isFile = true),
                    FolderItem("app.json", "Configuração geral do app Expo de permissões de microfone.", isFile = true),
                    FolderItem(
                        name = "src",
                        description = "Pasta principal de lógica.",
                        isFile = false,
                        children = listOf(
                            FolderItem(
                                name = "components",
                                description = "Componentes visuais de avatar, salas públicas e chat.",
                                isFile = false,
                                children = listOf(
                                    FolderItem("DojoGrid.tsx", "Renderiza o espaço isométrico das academias do dojo.", isFile = true),
                                    FolderItem("VoiceIndicator.tsx", "Exibe status WebRTC do microfone.", isFile = true)
                                )
                            ),
                            FolderItem(
                                name = "hooks",
                                description = "Hooks para lidar com conexões e estados.",
                                isFile = false,
                                children = listOf(
                                    FolderItem("useSocket.ts", "Controla conexão com Socket.IO e sincronização de posições do avatar.", isFile = true, sampleCode = """import { useEffect, useState } from 'react';
import { io, Socket } from 'socket.io-client';

export const useSocket = (roomId: string, token: string) => {
  const [socket, setSocket] = useState<Socket | null>(null);

  useEffect(() => {
    const socketInstance = io('https://api.jiuverse.com/gameserver', {
      auth: { token },
      transports: ['websocket'],
      query: { roomId }
    });

    setSocket(socketInstance);

    return () => {
      socketInstance.disconnect();
    };
  }, [roomId, token]);

  return socket;
};"""),
                                    FolderItem("useWebRTC.ts", "Cria canal de WebRTC de voz por proximidade (mesh bidirecional).", isFile = true)
                                )
                            )
                        )
                    )
                )
            ),
            FolderItem(
                name = "backend-nest",
                description = "Servidor modular em NestJS fornecendo REST API e microserviço de WebSockets confiável.",
                isFile = false,
                children = listOf(
                    FolderItem("package.json", "Contém NestJS core, @nestjs/websockets, sqlite/postgresql prisma client.", isFile = true),
                    FolderItem("prisma", "Definições locais do banco de dados e migrações.", isFile = false, children = listOf(
                        FolderItem("schema.prisma", "Código de modelagem do prisma.", isFile = true)
                    )),
                    FolderItem(
                        name = "src",
                        description = "Pastas de lógica do servidor.",
                        isFile = false,
                        children = listOf(
                            FolderItem(
                                name = "gateways",
                                description = "Servidor Websockets (Socket.IO) escalável que processa movimentações autoritativas e chat.",
                                isFile = false,
                                children = listOf(
                                    FolderItem("game.gateway.ts", "Controla tickrate de posições e validações de anti-cheat.", isFile = true, sampleCode = """import { SubscribeMessage, WebSocketGateway, WebSocketServer, OnGatewayConnection } from '@nestjs/websockets';
import { Server, Socket } from 'socket.io';
import { UseGuards } from '@nestjs/common';
import { WsJwtGuard } from '../auth/guards/ws-jwt.guard';

@WebSocketGateway({ namespace: 'gameserver', cors: true })
export class GameGateway implements OnGatewayConnection {
  @WebSocketServer() server: Server;
  
  // Limitação de tickrate de posições a 10Hz (100ms) por cliente
  private lastUpdateTokenMap = new Map<string, number>();

  async handleConnection(client: Socket) {
    const roomId = client.handshake.query.roomId;
    client.join(roomId);
    console.log(`Cli conectado: ${'$'}{client.id} em ${'$'}{roomId}`);
  }

  @UseGuards(WsJwtGuard)
  @SubscribeMessage('move')
  handleMovement(client: Socket, payload: { x: number; y: number }) {
    const now = Date.now();
    const lastUpdate = this.lastUpdateTokenMap.get(client.id) || 0;
    
    // Protege contra spams de conexões / Speed Hacks por Tickrate anormal
    if (now - lastUpdate < 80) return; // Mínimo de 80ms entre atualizações (12Hz máximo)
    
    this.lastUpdateTokenMap.set(client.id, now);
    const roomId = Array.from(client.rooms)[1]; // Sala principal simulada
    
    // Servidor repassa movimentação com carimbo de validação
    client.to(roomId).emit('playerMoved', { id: client.id, x: payload.x, y: payload.y });
  }
}""")
                                )
                            ),
                            FolderItem(
                                name = "voice",
                                description = "Sinalização de WebRTC para comunicação por voz por proximidade (Sinalização Mesh / SFU).",
                                isFile = false,
                                children = listOf(
                                    FolderItem("signaling.controller.ts", "Oferece rotas POST e Socket de troca de SDP Offer, Answer e canditatos ICE.", isFile = true)
                                )
                            )
                        )
                    )
                )
            ),
            FolderItem(
                name = "infra-docker",
                description = "Configs de orquestração local, balanceamento de carga e servidores de voz TURN.",
                isFile = false,
                children = listOf(
                    FolderItem("docker-compose.yml", "Levanta os containers do PostgreSQL, Redis cluster, Nginx e coturn (TURN/STUN).", isFile = true),
                    FolderItem("nginx.conf", "Faz o proxy reverso e balanceia conexões WebSocket (Socket.IO) dividindo de forma Sticky (IP hash) para vários servidores NestJS.", isFile = true, sampleCode = """upstream nest_backend {
    ip_hash; # Sticky Session obrigatório para Websockets!
    server nestjs_app_1:3000;
    server nestjs_app_2:3000;
}

server {
    listen 443 ssl;
    server_name api.jiuverse.com;

    ssl_certificate /etc/ssl/jiuverse.crt;
    ssl_certificate_key /etc/ssl/jiuverse.key;

    location / {
        proxy_pass http://nest_backend;
        proxy_http_version 1.1;
        proxy_set_header Upgrade ${'$'}http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host ${'$'}host;
    }
}"""),
                    FolderItem("coturn.conf", "Configurações de rede, portas udp/tcp e segredos para o Coturn TURN Server em produção.", isFile = true)
                )
            )
        )
    )

    val incidents = listOf(
        PlaybookIncident(
            id = "ddos",
            title = "Ataque DDoS ao Gateway",
            attackType = "Infraestrutura",
            mitigationTech = "Cloudflare + Nginx Rate Limiter",
            description = "Uma botnet coordena 50.000 requisições por segundo para sobrecarregar a API HTTP/Websockets de autenticação.",
            simulationLogs = listOf(
                "BOTNET: Enviando 15k requisições por IP na porta 443...",
                "CLOUDFLARE: Desafio JS (WAF) ativo. Detectando volumetria de IPs não residenciais.",
                "CLOUDFLARE: 80% das conexões maliciosas bloqueadas na borda.",
                "NGINX: Rate limit ativo (ngx_http_limit_req_module) em 50 req/m por IP.",
                "NGINX: Retornando HTTP 429 para requisições abusivas que vazaram da borda.",
                "DOCKER / PM2: Escalonamento automático por uso de CPU em 75% disparado (+2 pods de NestJS ativos).",
                "SISTEMA AUTO-MITIGADO: Serviços ativos e saudáveis para usuários reais."
            )
        ),
        PlaybookIncident(
            id = "speedhack",
            title = "Trapaça de Movimento (Speed Hack)",
            attackType = "Anti-Cheat",
            mitigationTech = "Modo Servidor Autoritativo + Tick Counter",
            description = "Modificação de memória do pacote para enviar 100 eventos de movimentação por segundo, tentando mover-se mais rápido em pixel rate.",
            simulationLogs = listOf(
                "CHEAT CLIENT: Enviando coordenadas: X=10.0, Y=10.0... X=50.0, Y=120.0 em 5 milissegundos.",
                "NESTJS GATEWAY: Pacote de 'move' recebido de Conexão ID #cl_31a.",
                "NESTJS GATEWAY: Rodando Validação de Delta T: Tempo desde última msg = 5ms (Limite min: 80ms).",
                "NESTJS GATEWAY: Tick rate abusivo de cliente detectado (Excedeu 12Hz limite).",
                "NESTJS REACTION: Bloqueando processamento do pacote de movimentação.",
                "NESTJS REACTION: Calculando vetor de distância de salto entre frames: DeltaD > MaxVel.",
                "NESTJS GATEWAY: Coordenada suspeita rejeitada no servidor. Forçando spawn de volta para X=10.0, Y=10.0.",
                "REDIS AUDIT: Incrementando indicador de fraude de #cl_31a no cache.",
                "ADMIN MODERATION: Alerta disparado para equipe interna se atingir 10 rejeições em 5 minutos."
            )
        ),
        PlaybookIncident(
            id = "spambots",
            title = "Inundação de Chat por Bots (Spam)",
            attackType = "Anti-Spam",
            mitigationTech = "Token Bucket (Redis) + Regex/Fuzzy Matching",
            description = "Robôs repetem consecutivamente 'COMPRE JIUCOINS BARATO EM SCAM.COM! ! !' a cada meio segundo em salas públicas.",
            simulationLogs = listOf(
                "BOT CLIENT: Enviando chat: 'COMPRE JIUCOINS BARATO EM SCAM.COM!'",
                "REDIS CACHE: Chamando barreira de Token Bucket do ID #cl_99b. Tokens atuais: 5.",
                "REDIS CACHE: Token consumido para chat. Tokens restantes: 4.",
                "NESTJS MODERATION SERVICE: Executando triagem regex e similaridade de Levenshtein.",
                "NESTJS COMPARATOR: Texto semelhante a padrão de spam conhecido ('SCAM.COM'). Coincidência de 92%.",
                "NESTJS AUDITOR: Bloqueando propagação do chat para os outros clientes na sala.",
                "BOT CLIENT: Enviando chat repetitivo novamente em 150ms...",
                "REDIS CACHE: Token Bucket esvaziado. Retornando erro 429 nos sockets internos.",
                "NESTJS AUTOMATION: Ativando Shadowban silencioso para #cl_99b. Ele acha que está falando, mas ninguém lê."
            )
        ),
        PlaybookIncident(
            id = "moderation_audio",
            title = "Denúncia de Chat de Voz Tóxico",
            attackType = "Moderação",
            mitigationTech = "Áudio Buffer Circular Local + Auditoria Central",
            description = "Um usuário profere ofensas no canal de voz WebRTC por proximidade. Um jogador que ouviu aperta 'Denunciar'.",
            simulationLogs = listOf(
                "CLIENT: Usuário 'ProPlayer99' emitindo ondas sonoras abusivas.",
                "VICTIM: Vítima clica no botão com ícone de 'Denunciar' sobre o avatar de 'ProPlayer99'.",
                "EXPO (CLIENT VICTIM): Grava micro-clip automático dos últimos 8 segundos de áudio do canal WebRTC associado (armazenado em buffer circular local).",
                "EXPO: Codificando áudio local em OGG decodificado e fazendo upload seguro via POST para NestJS API /moderation/voice-report.",
                "NESTJS API: Salvando relatório de áudio, ID do denunciante, ID do agressor e do quarto em bucket S3.",
                "ADMIN WEB PANEL: Relatório inserido na fila de pendências dos Moderadores ativos com espectrograma e player de áudio instantâneo.",
                "MODERATOR: Escuta áudio de 8s e confirma toxicidade verbal extrema.",
                "NESTJS CONTROL: Envia evento real-time via Socket.IO mutando permanentemente a capacidade de voz de 'ProPlayer99' e suspendendo sua conta em 7 dias."
            )
        )
    )

    val roadmapPhases = listOf(
        RoadmapPhase(
            id = "mvp",
            title = "Fase 1: Fundação MVP & Sincronia Real-Time",
            subtitle = "Semanas 1 a 6 (Time: 3 Devs) — Setup de Infraestrutura básica e avatares",
            duration = "6 semanas",
            team = "3 Devs (1 Front, 1 Back, 1 DevOps/Sênior)",
            tasks = listOf(
                "Desenho do banco Postgres via Prisma e setup do Docker Compose básico.",
                "Backend NestJS com REST JWT login/auth e modulo Socket.IO básico.",
                "Frontend Expo com motor simples de Tilemap 2.5D (grid ortogonal/isométrico).",
                "Movimentação interpolada simples e chat texto local da sala em 50ms de latência."
            ),
            deliverables = listOf(
                "Dois clones sincronizados andando no navegador e no celular na mesma sala privada.",
                "Autenticação de usuários resiliente a falhas e cache offline local de sessões."
            ),
            risks = listOf(
                "Risco: Sobrecarga nos frames iniciais de renderização em telefones mais antigos.",
                "Mitigação: Renderizar via Canvas puro ou WebGL otimizado em vez de componentes React Native normais."
            )
        ),
        RoadmapPhase(
            id = "voice_custom",
            title = "Fase 2: Academias Editáveis & Voz Proximidade WebRTC",
            subtitle = "Semanas 7 a 14 (Time: 4 Devs) — Sistema de quartos criados e áudio por posição",
            duration = "8 semanas",
            team = "4 Devs (2 Front, 1 Back Sênior, 1 Áudio/WebRTC Expert)",
            tasks = listOf(
                "Integração do coturn local (TURN/STUN) na nuvem e lógica de canais WebRTC Mesh.",
                "Desenvolvimento do DojoGrid no Expo: arrastar e soltar móveis e salvar JSON do grid via Prisma.",
                "Cálculo matemático no cliente Expo para atenuar ganho de áudio baseado no vetor distância entre avatares.",
                "Implementação do sistema básico de XP, faixas de Jiu-Jitsu e níveis."
            ),
            deliverables = listOf(
                "Usuários criam suas salas de treino, decoram tatames e conversam por voz por proximidade (som fica mais baixo ao se distanciar)."
            ),
            risks = listOf(
                "Risco: Desempenho instável de conexões WebRTC Mesh se houver mais de 5 pessoas próximas (limite N*(N-1) conexões).",
                "Mitigação: Implementar um SFU (Selective Forwarding Unit) como LiveKit ou mediasoup nas salas que excederem o limite."
            )
        ),
        RoadmapPhase(
            id = "economy",
            title = "Fase 3: Economia, Loja & Marketplace Seguro P2P",
            subtitle = "Semanas 15 a 20 (Time: 4 Devs) — Moeda virtual e transferências de itens sem duplicidades",
            duration = "6 semanas",
            team = "4 Devs (1 Back Sênior focado em Financiamento/Segurança, 2 Front, 1 QA)",
            tasks = listOf(
                "Transações do banco protegidas por isolamento nível SERIALIZABLE do Postgres (Prisma \$transaction).",
                "Loja virtual com cosméticos e acessórios de avatar organizados por raridade e tags.",
                "Sistema de Marketplace P2P de leilão e ofertas, sem risco de exploits de duplicação de itens (Race Conditions).",
                "Módulo de Conquistas (Achievements) e sistema de eventos de XP duplo ao vivo controlados por cron."
            ),
            deliverables = listOf(
                "Controle de inventário íntegro, comércio seguro de faixas raras e tatames entre avatares com logs de auditoria."
            ),
            risks = listOf(
                "Risco: Ataques de race condition para duplicar itens vendendo-os no mesmo milissegundo de duas solicitações paralelas.",
                "Mitigação: Usar travas pessimistas (SELECT FOR UPDATE) ou Redis locks para serializar transações do mesmo inventário por ID."
            )
        ),
        RoadmapPhase(
            id = "scale",
            title = "Fase 4: Alta Disponibilidade, Escala 100k CCU & Auditoria",
            subtitle = "Semanas 21 a 26+ (Time: 5 Devs) — Distribuição mundial e testes de estresse intensivos",
            duration = "6+ semanas",
            team = "5 Devs (2 DevOps Sênior, 1 Architect Back-end, 1 SecOps, 1 QA)",
            tasks = listOf(
                "Configurar adaptador Redis Pub/Sub para propagação de sockets em múltiplos servidores NestJS por trás do Nginx.",
                "Implementação de CDN Cloudflare para proteção DDoS na beira e WebSockets Sticky Session.",
                "Policiamento de Shadowban para Spam, rate limits agressivos no Redis e moderação central de relatórios de voz.",
                "Execução de testes de estresse em clusters Kubernetes ou Docker auto-scaling simulando 100k conexões simultâneas (via K6 ou Artillery)."
            ),
            deliverables = listOf(
                "Infraestrutura de jogo altamente tolerante a falhas (Zero Down-time), proteção contra cheaters, robustez total."
            ),
            risks = listOf(
                "Risco: Gargalo de escrita de banco se 100k jogadores tentarem persistir posições X,Y a cada segundo.",
                "Mitigação: Salvar coordenadas dinâmicas apenas em memória Redis no segundo-plano e persistir no Postgres final apenas quando o avatar mudar de sala ou a cada 5 minutos."
            )
        )
    )
}
