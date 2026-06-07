# JIUVERSE: BLUEPRINT DE ARQUITETURA E DIRETRIZES DE GAME DESIGN 🥋🎮
### Parecer de Engenharia de Jogos e Design AAA (Perspectiva 2026)

**Autores:** Diretor de Arte AAA, Lead Game Designer, UX Designer e Arquiteto de MMORPGs Sociais.
**Destinatário:** Equipe de Engenharia do JiuVerse.
**Status Documento:** APROVADO / TÉCNICO.

---

## INTRODUÇÃO E ESCREVENTE DE DESIGN

Este documento formaliza as decisões de design sistêmico, direção de arte e arquitetura de software para o **JiuVerse MMORPG**. O objetivo principal é analisar os pilares ancestrais dos mundos virtuais isométricos bidimensionais — especificamente a linhagem técnica do Habbo Hotel, documentada nos repositórios e bases técnicas de referência (`Habbo Assets`, `Origins Furni`, `Habbend`, `Habbo Archive` e `Habborator Art`) — e adaptá-los para um ecossistema nativo moderno (Android, Jetpack Compose, Kotlin Coroutines, MVVM) focado na cultura, no vestuário e nas dinâmicas de treino do **Brazilian Jiu-Jitsu (BJJ)**.

> **MANDATO CRÍTICO DE DIREÇÃO DE ARTE:** 
> Fica estritamente **proibida** a cópia direta de ativos, reaproveitamento de spritesheets, reutilização de texturas, mobis de mobiliário ou formatos de avatares protegidos de terceiros. Toda a direção visual aqui descrita baseia-se na extração técnica dos *padrões matemáticos de organização espacial, densidade e fluxo*, traduzindo-os para uma IP proprietária, de alta fidelidade e autoral: o **JiuVerse**.

---

## PARTE 1: DESTRINCHAMENTO TÉCNICO DE REFERÊNCIAS VISUAIS

### 1. Habbo Assets (habboassets.com)
*   **Análise Técnica:** Focado no versionamento histórico e catalogação de metadados das sprites. Revela os sistemas de estruturação de sprites através de múltiplos arquivos PNG contendo fatias direcionais (8 direções tradicionais).
*   **Padrão Arquitetônico Extraído:** Sistema de mapeamento por IDs únicos estruturado em categorias rígidas (`FURNI`, `BADGE`, `EFFECT`, `FIGURE`).
*   **Direção para o JiuVerse:** Substituiremos a abordagem de sprites fatiados por renderização vetorial descritiva baseada em código e SVGs otimizados sob demanda direto no `Canvas` de Compose, aplicando o mesmo rigor de categorização para os kimonos (`UNIFORM`), faixas (`BELT`), assessórios esportivos (`ACCESSORY`) e emotes de respeito (`EMOTE`).

### 2. Origins Furni (originsfurni.dev)
*   **Análise Técnica:** Uma ferramenta que documenta as propriedades físicas, de colisão e manipulação de móveis na grade geométrica ("móveis antigos"). Mostra o empilhamento vertical clássico baseado em dados numéricos puros de altura (`z-height`).
*   **Padrão Arquitetônico Extraído:** Matrizes de ocupação bidisciplinares (x, y) com uma variável decimal real (z) controlando as alturas acumuladas para permitir sobreposição lógica exata de itens esportivos (ex: tatames sobrepondo decks metálicos, sacos de pancada suspensos sobre fixadores de teto).
*   **Direção para o JiuVerse:** Adoção de uma matriz `SolidObstacle` de tamanho flutuante que determina sombras dinâmicas e o algoritmo de oclusão de visão do jogador se posicionado atrás de grandes elementos (como armários de vestiário).

### 3. Habbend Downloads (habbend.com/tools/downloads)
*   **Análise Técnica:** Repositório de empacotamento, compilação de pacotes gráficos e decodificadores de ativos.
*   **Padrão Arquitetônico Extraído:** Entendimento das camadas de renderização ("Rendering States": Off, On, Interativo, Quebrado) de elementos ativos de sala de aula e dojo.
*   **Direção para o JiuVerse:** Criação de estados visuais reativos no Jetpack Compose para móveis interativos do JiuVerse. Por exemplo: o placar de tempo de rola (`scoreboard`) que alterna entre "Inativo", "Contagem Progressiva (Verde)" e "Finalizado (Flashing Vermelho)".

### 4. Habbo Archive (habboarchive.com)
*   **Análise Técnica:** Arquivo detalhado do fluxo histórico global de salas públicas e dojos.
*   **Padrão Arquitetônico Extraído:** O conceito de "Focal Point" (Ponto Focal) da sala — lobbies amplos que afunilam os avatares em zonas sociais bem destacadas (ex: a mureta ou área de estar central).
*   **Direção para o JiuVerse:** Estruturação dos Dojos do JiuVerse com um Tatame Central ampliado que age como receptor principal do chat, delimitado por faixas esportivas nas margens onde outros jogadores assistem ao combate de forma passiva.

### 5. Habborator Art (habborator.org/art)
*   **Análise Técnica:** Masterclass em Pixel Art Isométrica, detalhando a técnica clássica de "Outer Outlining" de 1px preto e "Inner Outlining" colorido para dar profundidade e volume a pixels de baixa resolução.
*   **Padrão Arquitetônico Extraído:** Direção de luz padrão (geralmente vinda do topo-esquerdo em diagonal descendente de 45°). Uso de paletas estilizadas de cores limitadas de alto contraste para destacar elementos funcionais.
*   **Direção para o JiuVerse:** Os elementos vetoriais e de Canvas desenhados de forma nativa no JiuVerse adotarão a luz diagonal top-left, combinando "Blueprint Slate" de fundo escuro com bordas externas marcantes para assegurar contraste AAA no mobile.

---

## PARTE 2: ANÁLISE PROFUNDA DOS 10 PILARES TÉCNICOS E DE GAME DESIGN

```
         Perspectiva Isométrica Dimétrica (2:1)
                       /\
                      /  \
     TileWidth (2x)  /    \  TileHeight (1x)
                    \      /
                     \    /
                      \  /
                       \/
```

### 1. Perspectiva Isométrica
*   **Estrutura de Referência:** Projeção Isométrica Dimétrica onde para cada 2 pixels horizontais há exatamente 1 pixel vertical (ângulo matemático de aproximadamente 26.56°).
*   **Fórmulas Matemáticas do JiuVerse:**
    Para traduzir coordenadas tridimensionais lógicas de grid do dojo $(x, y, z)$ para coordenadas bidimensionais de tela $(X, Y)$ em pixels:
    $$X_{screen} = (x - y) \cdot W_{half} + X_{offset}$$
    $$Y_{screen} = (x + y) \cdot H_{half} - (z \cdot Z_{step}) + Y_{offset}$$
    Onde $W_{half}$ é a metade da largura física do tile e $H_{half}$ é a metade da altura física do tile ($W_{half} / H_{half} = 2.0$).
*   **Z-Sorting (Painters' Algorithm):** Para evitar desordenação visual de camadas de renderização, os elementos da tela serão classificados em ordem crescente de profundidade através da fórmula de índice de desenho:
    $$\text{DrawPriority} = (x + y) \cdot 1000 + z$$
    Isso assegura que avatares posicionados nas coordenadas frontais do grid de tatame escondam mecanicamente as entidades de parede traseiras.

### 2. Escala dos Avatares
*   **Estrutura de Referência:** Antropomorfismo estilizado (cabeças proeminentes para leitura de expressões, quadris e pernas compactos para cabimento em células grid).
*   **JiuVerse Specs:**
    *   **Proporção:** 1:3.5 (Média de 3.5 cabeças para a altura total). Proporção esportiva robusta que valoriza ombros largos e pescoço reforçado de lutador de Jiu-Jitsu.
    *   **Dimensões:** Altura média do avatar em pé equivalente a 2.3 $H_{half}$ (aprox. 85-95 pixels de desenho do Canvas).
    *   **Pegada no Grid:** 1x1 tile físico do chão. O pé do avatar é ancorado no centro geométrico do tile ($x + 0.5, y + 0.5$).
    *   **8 Direções:** Mapeabilidade vetorial total do lutador girando nos eixos Norte, Nordeste, Leste, Sudeste, Sul, Sudoeste, Oeste, Noroeste.

### 3. Escala dos Móveis (Equipment & Dojos Decor)
*   **Estrutura de Referência:** Grid-aligned modularity. Mobília que ocupa exatamente múltiplos de $1 \times 1$ tile de grid.
*   **JiuVerse Specs:**
    *   **Ocupação:** Equipamentos como placares eletrônicos e armários são especificados em tamanho de matriz (ex: Placar lateral 1x2, Vestiário de Armários 2x2, Saco de Pancadas 1x1).
    *   **Ponto de Ancoragem:** Coordenada de grid mínima do item da mobília no canto superior do seu quadrante de colisão.
    *   **Z-Stacking:** Equipamentos esportivos menores (ex: faixas extras dobradas, garrafas de shake nutricional, toalhas, cartazes) podem ser empilhados sobre mesas ou prateleiras que contenham uma propriedade `StackHeightLimit` do tipo flutuante positiva.

### 4. Estrutura das Salas (Dojos & Academias)
*   **Estrutura de Referência:** Paredes modulares invisíveis ao norte que dão lugar a cortes transversais em ângulo isometrico padrão para evitar obstrução. Piso composto por uma matriz definida de coordenadas de relevo.
*   **JiuVerse Specs:**
    *   **Mapeamento de Relevo (Dojo Heightmap):** Uma matriz bi-dimensional de caracteres string (como utilizado no JiuVerse):
        ```
        0 0 0 x x x x x
        0 1 1 x y y y y
        0 1 1 0 0 0 2 2
        ```
        Onde `0` representa piso padrão de madeira de demolição, `1` representa elevação de 0.5m do tablado de Tatame de alta absorção, `2` representa área extrema suspensa da arquibancada e `x` / `y` representam fendas de parede intransponíveis ou portas laterais de interação de fuga de câmera.
    *   **Cutaway de Parede Inteligente:** Ao passar por trás de colunas de betão ou decorações de parede de clãs, o respectivo asset tem o seu canal de opacidade `Alpha` reduzido dinamicamente de `1.0f` para `0.3f`, mantendo o foco total na ação tática de finalização ou movimentação do avatar do lutador.

### 5. Fluxo de Navegação
*   **Estrutura de Referência:** Busca de rota interativa via cliques sobre o piso de projeção isométrica.
*   **JiuVerse Specs:**
    *   **Aprimoramento de Controles Nativos:** Integração do controle híbrido. Para navegação clássica, mantemos o mouse/tap que realiza a busca de novos nós de rota no grid usando o algoritmo de **Pathfinding A* assíncrono**. Para controle rápido competitivo, a movimentação do avatar responde diretamente aos eixos virtuais do Joystick de Tela (OverviewTab), mapendo vetores direcionais contínuos para as 8 direções discretas da perspectiva isométrica.
    *   **Entrada de Sala Não-Bloqueante:** O carregamento da sala de tatame do clã é processado com coroutines em background, liberando a interface do usuário local para exibir chat de canais globais sem desengajar o fluxo de jogabilidade competitiva.

### 6. Interface do Usuário (UX/UI de Jogo)
*   **Estrutura de Referência:** Displays de salas públicas amigáveis e console flutuante de mensagens diretas de amigos.
*   **JiuVerse Specs:**
    *   **Arquitetura Base:** UI desenhada inteiramente na filosofia Material 3 do Material Design do Google com tonalidades de cor e tipografia brutas/técnicas denominadas "Blueprint".
    *   **Cuidado com Notch e Safe Areas:** Aplicação estrita de insets dinâmicos (`WindowInsets.safeDrawing`, `.navigationBarsPadding()`) em todas as telas sobrepostas para que botões de golpes, canais de chat e placares esportivos não coincidam com a gota da câmera frontal ou os cantos inclinados dos aparelhos modernos de tela curva.
    *   **Transparências Otimizadas:** Uso substancial de preenchimento translúcido fosco e gradientes sutis para que a beleza geométrica do tatame e o movimento dos lutadores permaneçam visíveis por trás das placas de diálogo tático e painéis de configuração.

### 7. Organização dos Ambientes
*   **Estrutura de Referência:** Redes de canais públicos acessíveis por navegadores nativos unificados de salas virtuais.
*   **JiuVerse Specs:**
    *   **Lobby e Entrada de Clãs (The Academy Hub):** Onde múltiplos lutadores se encontram no mesmo Tatame Principal para agendar desafios, bater papo geral, exibir graduações e obter missões diárias com Mestres.
    *   **Lotes Territoriais (The Sandboxes):** Área para usuários personalizarem seus próprios Dojos, movendo tatames de cores variadas, sacos de areia, prateleiras de troféus esportivos conquistados e plantas orientais decorativas dinamicamente de forma persistente.
    *   **Instâncias Compartilhadas (Arena de Competição):** Zonas específicas onde duelos de passagem de guarda e chaves de pé são disputados com arquibancadas adjacentes ativas para espectadores que mandam emotes e torcem pelo chat público em tempo real.

### 8. Sistema de Interação Social
*   **Estrutura de Referência:** Chat bubble flutuante com rolagem contínua vertical e bolhas customizadas.
*   **JiuVerse Specs:**
    *   **Chat de Tatame Inteligente:** As mensagens enviadas nascem em balões semitransparentes centralizados e ancorados exatamente acima da cabeça do avatar emissor, subindo linearmente. Caso o espaço superior fique saturado por excesso de mensagens simultâneas de múltiplos atletas, o algoritmo do chat aplica forças de repulsão físicas virtuais para os eixos laterais (eixo x), evitando a sobreposição de textos.
    *   **Gestual Social Esportivo (Os Emotes com Impacto):** No JiuVerse, botões de ação e chat rápido ativam poses físicas icônicas de Jiu-Jitsu que influenciam na imersão:
        1.  *Cumprimento Oss! (Pose de Respeito):* Curvatura do tronco em 30 graus com braços paralelos ao corpo.
        2.  *Meditação Zen:* Avatar se senta no solo com as pernas cruzadas no tatame.
        3.  *Guarda de Combate:* Posição defensiva agachada com as mãos erguidas no centro da célula do grid.

### 9. Economia Visual
*   **Estrutura de Referência:** Utilização inteligente de linhas e cores para manter a legibilidade imediata das mobílias raríssimas.
*   **JiuVerse Specs:**
    *   **Destaque do Item:** Cada tatame e elemento visual do JiuVerse possui graus de categorização comuns, raros, épicos, e lendários (M3 Colors integrados no `AvatarTab`).
    *   **Contraste de Ação:** O chão e as grades estruturais são desenhados de forma subexposta (cores neutras, escuras e foscas - Slate e Gray). Equipamentos interativos chaves (ex: o tatame de treino de combate ativo) brilham com bordas de neon azulado ou laranja de alta intensidade (BlueprintCyan ou BlueprintOrange), facilitando a identificação imediata das zonas onde há desafios abertos.

### 10. Hierarquia de Informações
*   **Estrutura de Referência:** Badges pequenos e cartões de visita de jogador.
*   **JiuVerse Specs:**
    *   **A Tríade da Identidade Esportiva:** Cada jogador é visualmente avaliado no topo do seu avatar em uma pilha de tags flutuantes organizadas de forma vertical perfeita:
        1.  *A Faixa de Graduação:* Um indicador de barra contendo a cor de sua faixa atual (Branca, Azul, Roxa, Marrom, Preta, Coral).
        2.  *O Nome de Lutador e Prefixo de Clã (Dojo):* Renderizado com peso e contraste ideais para leitura mesmo em alto zoom.
        3.  *Badge de Trofeu Ativo:* Um pequeno ícone de vitória em campeonatos de JiuVerse que cintila quando o lutador entra na sala de aula.

---

## PARTE 3: PARECER DE ADAPTAÇÃO, RECRIÇÃO E MODERNIZAÇÃO 2026

### O que pode ser adaptado (Adapt)
*   **A perspectiva diométrica isométrica de 2:1:** Mantém o clássico charme e a sensação nostálgica de comunidade que permitiu e ainda permite excelente navegabilidade em visualizações espaciais.
*   **A estrutura de layout em grade celular cartesiana estrita (Grids):** Fundamental para planejar posições de tatames, equipamentos de musculação, colisões e otimizar as consultas espaciais de pathfinding.
*   **O sistema de Z-Sorting (Pintores clássicos de tela):** Altamente adaptável à arquitetura de pintura reativa Compose por ser leve e eficaz.

### O que deve ser recriado (Recreate)
*   **Renderização Vector/Canvas Baseada em Código:** Abandonar spritesheets estáticas gigantescas que pesam na memória do celular. No JiuVerse, desenhamos os corpos, kimonos, faixas e cortes de cabelo diretamente com polígonos, caminhos vetoriais com antialiasing e preenchimento de gradientes reativos no `Canvas` Jetpack Compose de 60/120 FPS.
*   **Estruturação de UI Nativa Material 3:** Eliminar as antigas janelas e popups flutuantes baseadas em imagens cinzas estáticas. Criar interfaces responsivas com flexibilidade tátil e fluidez de toque baseadas puramente no Material Theme 3.
*   **Motor de Pathfinding Assíncrono com Kotlin Coroutines:** Substituir pathfindings síncronos e lentos do passado por resoluções dinâmicas concorrentes e não-bloqueantes de trajetos sobre a matriz espacial.

### O que deve ser evitado (Avoid)
*   **Uso de WebViews para Games-Loops de Renderização no Mobile:** Misturar WebView com motor Web para renderizar displays isométricos gera latência e consome bateria excessivamente no Android. A renderização do JiuVerse deve ocorrer de forma 100% nativa em Compose Canvas.
*   **Elementos Gráficos sem Borda de Contraste:** Móveis e avatares que se misturam ao chão escuro causam fadiga visual e reduzem a acessibilidade para pessoas com deficiência visual.
*   **Controles Travados e Ausência de Ajuste de Notch:** Telas que cortam elementos perto da área da câmera frontal criam um visual de app mal acabado e de baixo orçamento.

### Como modernizar para 2026 (Modernize 2026)
*   **Gráficos HD Vector Pixel Art de Alta Densidade:** Vetorização rasterizada sob demanda matemática que mantém a essência clássica dos quadradinhos nas linhas diagonais mas perfeitamente escalável em displays 4K sem borrar.
*   **Dojos Generativos com Inteligência Artificial Gemini (Sensei AI):** NPCs dinâmicos integrados com o Gemini API que respondem a orientações táticas de luta na sala de aula em tempo real (veja `SenseiAiTab.kt`).
*   **Áudio Espacial e Proximidade por Voz (Proximity Audio Channels):** O chat por voz e o volume das mensagens de áudio variam em tempo real de acordo com a distância matemática isométrica entre o seu avatar e os outros atletas (veja `VoiceProximityTab.kt`).

---

## PARTE 4: INTEGRAÇÃO DIRETA COM A BASE DE CÓDIGO DO JIUVERSE

### 🟢 Mapeamento da Arquitetura Atual e Conexões Técnicas

1.  **AvatarTab.kt (Personalização de Identidade):**
    *   *Ponto de Integração:* É o coração do módulo de renderização vetorial no Canvas do dojo. Adaptar as diretrizes de escala dos personagems Chibi/HD aqui estruturado.
    *   *Uso das Diretrizes:* Aproveitar o estado de postura `CONFIANTE` recém-implementado nas 4 direções (Frente, Costas, Perfil Esquerdo, Perfil Direito) e estender a renderização para exibir os novos gradientes M3 para kimonos e acessórios como os dojos customizáveis (ex: Relógio Inteligente de Luta `a6` e Cicatriz de Sparring `a5`).

2.  **OverviewTab.kt (Mapa Geral de Navegação):**
    *   *Ponto de Integração:* Contém a fórmula matemática de transformação inversa de cliques na tela para pontos de grid do terreno isométrico.
    *   *Uso das Diretrizes:* Elevar seu motor de busca aplicando o algoritmo A* adaptado deste blueprint para desviar dos obstáculos e das mobílias do Tatame sem cruzamento de paredes.

3.  **AcademyTab.kt (Visualização do Dojo Carlson Gracie):**
    *   *Ponto de Integração:* Representação das salas físicas do clã em posições percentuais coordenadas.
    *   *Uso das Diretrizes:* Implementar o suporte a corte transversal de paredes (cutaway de opacidade) quando o avatar avançar nas paredes norte do tatame ou da secretaria da escola.

4.  **LandSandboxTab.kt e LandscapeSandboxTab.kt (Criação de Dojos UGC):**
    *   *Ponto de Integração:* Sistemas que gerem layouts espaciais proprietários de terrenos isométricos.
    *   *Uso das Diretrizes:* Aplicação direta dos limites decimais de altura para garantir stacking de tatames sem sobreposições irregulares e anti-trapaça estrito de posicionamento territorial.

5.  **VoiceProximityTab.kt (Áudio de Proximidade):**
    *   *Ponto de Integração:* Processamento de atenuação de voz baseado na distância cartesiana.
    *   *Uso das Diretrizes:* Alinhar o vetor de distância $d(p_1, p_2) = \sqrt{(x_1-x_2)^2 + (y_1-y_2)^2}$ extraído da matriz isométrica lógica de coordenadas ao invés da distância visual da tela em pixels, garantindo consistência sonora independentemente do zoom aplicado à tela do usuário.

---

## PARTE 5: PLANO DE IMPLEMENTAÇÃO E CRONOGRAMA DE ENGENHARIA

O plano a seguir define os entregáveis sequenciais sem interrupção do pipeline de desenvolvimento do time de engenharia da JiuVerse:

```
                  ┌─────────────────────────────────────┐
                  │ FAASE 1: MOTOR DE COORDENADAS (S0-S2)│
                  └──────────────────┬──────────────────┘
                                     ▼
                  ┌─────────────────────────────────────┐
                  │ FASE 2: RENDER VETORIAL DE AVATARES │
                  └──────────────────┬──────────────────┘
                                     ▼
                  ┌─────────────────────────────────────┐
                  │  FASE 3: MOBÍLIAS E COLLISION GRID   │
                  └──────────────────┬──────────────────┘
                                     ▼
                  ┌─────────────────────────────────────┐
                  │ FASE 4: CHAT INTELIGENTE E SOCIALS  │
                  └──────────────────┬──────────────────┘
                                     ▼
                  ┌─────────────────────────────────────┐
                  │   FASE 5: INTEGRAÇÃO SENSEI E IA    │
                  └─────────────────────────────────────┘
```

### 🗓️ Cronograma Detalhado e Entregáveis por Sprint

#### Fase 1: Padronização Matemática do Motor de Coordenadas (Sprint 1-2)
*   **Escopo:** Implementação da biblioteca matemática de transformações isométricas e Z-Sorting bidirecional unificado na pasta nobre de modelos do repositório (`com.example.architecture.model`).
*   **Entregáveis:**
    *   `IsometricMathUtils.kt` operando com representação tridimensional flutuante.
    *   Modelagem da classe `GridMapTile` contendo propriedades de material (madeira, tatame, cimento) e atrito físico de caminhada.
    *   *Testes de Validação:* Testes automatizados unitários JUnit garantindo que a transformação inversa retorne as coordenadas lógicas originais com no máximo $0.001$ de margem de erro.

#### Fase 2: Motor de Renderização Vetorial de Avatares no Canvas (Sprint 3-4)
*   **Escopo:** Migração completa de todos os avatares das telas para o renderizador programático otimizado em Compose Canvas em substituição a arquivos de imagem estáticos pesados.
*   **Entregáveis:**
    *   Atualização integral e polimento do `AvatarTab.kt` para aceitar animação dinâmica baseada em chaves reativas de frame.
    *   Novos moldes vetoriais para kimonos amassados com sombras e as faixas de graduação oficiais.
    *   *Testes de Validação:* Verificação de FPS do aplicativo buscando métricas estáveis acima dos 90 FPS em hardware intermediário de teste.

#### Fase 3: Estrutura de Mobílias e Grid de Colisões do Dojo (Sprint 5-6)
*   **Escopo:** Criação do repositório lógico de móveis proprietários no clã e da base local do banco SQLite (via Room Integration) para carregar posições de tatames customizados persistentes dos jogadores.
*   **Entregáveis:**
    *   Integração do Room Database modelando a entidade persistente `DojoFurnitureEntity`.
    *   Módulo de arrastar e soltar itens (`DragAndDropHandler`) para edição do Dojo na aba de Sandbox.
    *   *Testes de Validação:* Cobertura de teste local simulando transações inválidas de sobreposição de tatames na mesma coordenada de relevo de grid (anti-trapaça).

#### Fase 4: Chat por Proximidade e Sistemas Sociais Estendidos (Sprint 7-8)
*   **Escopo:** Acoplamento do sistema de envio de balões físicos repelentes sobre a cabeça dos avatares e conexão de streaming com canal de voz espacializável na aproximação isométrica.
*   **Entregáveis:**
    *   Componente `FloatingIsometricChatBubble.kt` flutuando na viewport.
    *   Cálculo automático de decibéis de volume de chamadas em `VoiceProximityTab.kt` interconectado ao grid lógico tridimensional.
    *   *Testes de Validação:* Testes funcionais simulando 20 avatares simultâneos enviando textos no Tatame Central Carlson Gracie garantindo ausência de quedas de frames e sobreposição de letras.

#### Fase 5: Conectividade Cognitiva NPC de Inteligência Artificial (Sprint 9-10)
*   **Escopo:** Fornecimento dos assistentes automáticos de dojo acoplados ao Gemini API. NPCs Sensei reativos que conversam sobre técnicas de luta específicas a partir de modelos treinados de Jiu-Jitsu.
*   **Entregáveis:**
    *   Ponte reativa para REST API no `SenseiAiTab.kt` carregando respostas otimizadas.
    *   Status flutuante de reação do NPC (balão de pensamento com carregamento pulsante no Canvas).
    *   *Testes de Validação:* Validação de segurança de chave de API e detecção de exceções em conexões offline de rede móvel (graceful recovery).

---
## CONCLUSÃO E ASSINATURA DE DESIGN

Ao consolidar a estética histórica isométrica com os poderes de renderização instantânea do **Jetpack Compose** e a infraestrutura segura do ecossistema Android, o **JiuVerse** se consolida como um blueprint técnico definitivo de como transicionar e modernizar o game design nostálgico dos MMORPGs clássicos do início dos anos 2000 na arena competitiva e social de 2026.

Este documento apoia integramente o fluxo produtivo e a qualidade das entregas do time de engenharia de software da JiuVerse, garantindo que cada componente de layout represente a excelência e a nobreza de nossa arte marcial. **OSS!** 🥋🏆
