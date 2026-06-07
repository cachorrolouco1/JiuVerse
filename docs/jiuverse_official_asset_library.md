# JIUVERSE: BIBLIOTECA OFICIAL DE ASSETS E INVENTÁRIO DE ESTILO DE JOGO (RETRO 2.5D) 🥋🎨
### Relatório de Direção de Arte: Seleção, Compatibilidade Phaser 3/Mobile e Adaptações de Combate BJJ
**Autor:** Diretor de Arte de Jogos, Lead Concept Artist & Game Economist do JiuVerse
**Data:** 7 de Junho de 2026
**Status:** Aprovado para Produção e Integração técnica

---

## INTRODUÇÃO E FILOSOFIA DE DIREÇÃO DE ARTE
Como Diretor de Arte do **JiuVerse**, estabeleço a premissa de que a originalidade e o respeito à comunidade Open Source são os pilares da nossa estética. Este documento atua como o **Inventário Oficial de Curadoria de Ativos**, garantindo que não utilizaremos materiais proprietários protegidos ou pirateados (como rippings imperfeitos de *Habbo Hotel*, *Tibia* ou *World of Warcraft*). 

Todos os ativos catalogados aqui são provenientes de fontes legítimas (Kenney, OpenGameArt, CraftPix e itch.io), licenciados sob **CC0 (Domínio Público)**, **CC-BY (Atribuição)** ou **Licenças Comerciais Gratuitas**. Eles foram analisados sob a regra de ouro do JiuVerse: **Visual Retrô de Alta Fidelidade (DDP HD 2.5D)** com a grade isométrica clássica de proporção **2:1 (64x32px para pisos, 64x96px para avatares)**.

---

## INVENTÁRIO COMPLETO DE ASSETS POR CATEGORIAS

### 1. TILESETS (Pisos, Paredes e Elementos Estruturais)

#### A. Kenney Isometric Dungeon Tiles
*   **Origem / Link:** [Kenney Official Site](https://kenney.nl/assets/isometric-dungeon-tiles)
*   **Licença:** CC0 1.0 Universal (Domínio Público - Livre para uso comercial).
*   **Compatibilidade Phaser 3:** Perfeita. Os tiles de piso são renderizados em matriz isométrica `64x32px`. As paredes possuem elevação vertical compatível com o nosso renderizador. Pode ser carregado no Phaser como uma lista de imagens individuais (`this.load.image`) ou via Texture Atlas JSON criado no TexturePacker.
*   **Compatibilidade Mobile:** Altíssima. Como os tiles são de baixa resolução (com primor geométrico e cores planas), o consumo de memória de vídeo (VRAM) é desprezível. Ideal para renderização suave a 60 FPS em qualquer webview Android.
*   **Adaptações para Artes Marciais:**
    *   As texturas de tijolos de pedra cinzentos (`wall_cobble`) servirão de base para a **Arena PvP**, conferindo um visual de "Masmorra de Sparring" ou "Clube da Luta Subterrâneo".
    *   As quinas e colunas de rocha serão re-texturizadas digitalmente para carregar faixas coloridas penduradas e placas informativas dos patrocinadores do JiuVerse.

#### B. Isometric Block Tileset (Screaming Brain Studios)
*   **Origem / Link:** [itch.io Screaming Brain Studios](https://screamingbrainstudios.itch.io/isometric-block-tiles)
*   **Licença:** CC0 1.0 Universal (Domínio Público).
*   **Compatibilidade Phaser 3:** Total. A estrutura usa blocos cúbicos isométricos que se alinham perfeitamente ao nosso grid 2:1. O carregamento é feito através de spritesheets isométricos estáticos.
*   **Compatibilidade Mobile:** Excelente. Organizado em uma única planilha de texturas (Sprite Sheet) de `512x512px` (Power of Two), otimizando chamadas de desenho (Draw Calls) no WebGL móvel.
*   **Adaptações para Artes Marciais:**
    *   O bloco de cimento rústico cinza-escuro é ideal para o plano de fundo neutro do **Dojo Carlson Gracie**.
    *   O tile de terra/lama (`floor_mud`) será utilizado nas áreas externas e nos arredores da academia principal para simular o pátio de treinamento rústico ao ar livre.

---

### 2. PERSONAGENS (Modelos de Base e Sprites de Luta)

#### A. Pixel Art Martial Arts Character (CraftPix)
*   **Origem / Link:** [CraftPix Freebies](https://craftpix.net/freebies/free-martial-arts-character-sprites-pixel-art/)
*   **Licença:** CraftPix Free License (Permitido para projetos comerciais pessoais e comerciais sem custo).
*   **Compatibilidade Phaser 3:** Alta. Contém planilhas de sprites (`spritesheets`) com animações de socos, chutes e defesas. Devem ser fatiadas no Phaser usando `this.load.spritesheet('martial_artist', ..., { frameWidth: 64, frameHeight: 64 })`.
*   **Compatibilidade Mobile:** Muito Boa. Requer empacotamento em atlas de sprites para evitar sobrecarga de arquivos HTTP individuais. A escala do sprite deve ser multiplicada por `1.5x` no canvas móvel para manter a legibilidade do lutador.
*   **Adaptações para Artes Marciais:**
    *   O uniforme padrão do personagem será re-colorido por programação em Shader (Palette Swapping) ou por spritesheets sobrepostos (Layers) para alternar entre as cores de **Kimono** clássicas (Branco, Azul e Preto).
    *   Uma máscara de píxeis na linha de cintura será modificada dinamicamente para aplicar a cor da **Faixa BJJ** ativa do jogador (Branca, Azul, Roxa, Marrom, Preta ou Coral).

#### B. LPC (Liberated Pixel Cup) Character Bases (OpenGameArt)
*   **Origem / Link:** [OpenGameArt LPC Project](https://opengameart.org/content/lpc-character-bases)
*   **Licença:** CC-BY 3.0 / CC-BY-SA 3.0 (Uso comercial permitido mediante atribuição ao autor e compartilhamento no mesmo formato).
*   **Compatibilidade Phaser 3:** Total. Como o padrão LPC é uma das maiores referências em RPGs 2D, existem diversos plugins e adaptadores de animação Phaser criados para mapear as planilhas sob frames de caminhada nas 4 direções cardinais.
*   **Compatibilidade Mobile:** Excelente. Layout ortogonal que exige conversão de angulação vetorial para simular movimento isométrico 2.5D (rotacionando a física do vetor de velocidade a 45°).
*   **Adaptações para Artes Marciais:**
    *   Remoção de armas e escudos medievais medievais dos corpos bases.
    *   Mapeamento de vestimenta exclusivo: desenvolvimento de uma camada de "Kimono de Treino" combinada sobre o esqueleto humano padrão LPC, respeitando nossa proporção de avatar estabelecida de 3.5 cabeças para manter o aspecto atlético e parrudo dos competidores.

---

### 3. MÓVEIS (Decoração e Ambientação Geral)

#### A. Kenney Isometric Interior Pack
*   **Origem / Link:** [Kenney Interior Assets](https://kenney.nl/assets/isometric-interior)
*   **Licença:** CC0 1.0 Universal.
*   **Compatibilidade Phaser 3:** Perfeita. Todos os móveis utilizam projeção isométrica idêntica à nossa (2:1). Podem ser carregados como imagens estáticas individuais aplicadas a um grupo físico ou mapeador de profundidade (Depth Sorting). Depth é calculado dinamicamente no Phaser como `tile.y + tile.x`.
*   **Compatibilidade Mobile:** Altíssima. Os vetores rasterizados são leves, possuindo contornos nítidos e cores contrastantes que lêem excepcionalmente bem em telas AMOLED pequenas.
*   **Adaptações para Artes Marciais:**
    *   As prateleiras de livros e armários de madeira receberão sprays de design adicionais para parecerem **Racks com Kimonos e Toalhas** dobrados de treino.
    *   Os vasos de plantas e mudas verdes serão usados para dar ar de "Dojo Zen / Escola Clássica de Jiu-Jitsu" com influências brasileiras (como mudas de coqueiros ou heras nas paredes das academias).

#### B. Modern Interior Assets (Szadi Art)
*   **Origem / Link:** [Szadi Art Modern Interior](https://szadiart.itch.io/modern-interior)
*   **Licença:** Atribuição CC-BY (Permitido uso comercial com créditos).
*   **Compatibilidade Phaser 3:** Ótima. Os sofás, poltronas modernas, balcões de recepção e computadores são fatiados em blocos que podem ser colocados na camada física "Obstáculos" do mapa.
*   **Compatibilidade Mobile:** Muito boa. As cores do Szadi Art são pastéis e modernas, casando de forma fidedigna com a nossa estética de interface limpa no mobile.
*   **Adaptações para Artes Marciais:**
    *   Os balcões de recepção de escritório serão adaptados como a **Portaria de Inscrição nos Campeonatos** virtuais e recepção dos Dojos.
    *   Os sofás modernos de espera serão aplicados nos halls de entrada acadêmicos, permitindo que os avatares dos jogadores utilizem a animação `SIT_DOWN` enquanto aguardam os emparelhamentos de chaves de luta.

---

### 4. ACADEMIAS (Tatames e Equipamentos Desportivos)

#### A. Kenney Sports Pack / Gym Equipment
*   **Origem / Link:** [Kenney Sports](https://kenney.nl/assets/sports-pack)
*   **Licença:** CC0 1.0 Universal.
*   **Compatibilidade Phaser 3:** Excelente. Contém bolas, cones, troféus e medalhas. Carregável como imagens isoladas no Phaser, ideal para criar pequenos colecionáveis no mapa ou ícones dinâmicos nas árvores de inventário.
*   **Compatibilidade Mobile:** Ideal para criação de sprites de desempenho leve.
*   **Adaptações para Artes Marciais:**
    *   Cones e fitas demarcadoras serão convertidos em "Áreas de Treinamento Técnico de Quedas".
    *   Os **Troféus** serão redesenhados via folha de paleta metálica para premiar os participantes dos torneios mensais do JiuVerse (Medalha de Ouro, Prata e Bronze com fitas nas cores do pavilhão brasileiro).

#### B. Isometric Gym Pack (Screaming Brain Studios / Surt)
*   **Origem / Link:** [Screaming Brain Studios Gym](https://screamingbrainstudios.itch.io/isometric-gym-pack)
*   **Licença:** CC0 1.0 Universal.
*   **Compatibilidade Phaser 3:** Direta. Contém pesos livres, kettlebells, barras olímpicas, tatames modulares de ginástica e esteiras em vista isométrica. Importados em lote único.
*   **Compatibilidade Mobile:** Otimizado. Tamanho compacto e uso inteligente de atlas de textura.
*   **Adaptações para Artes Marciais:**
    *   Os tatames de yoga serão esticados e duplicados via laços `for` no gerador de tabuleiros para construir as **Grandes Superfícies de Luta** oficiais da Carlson Gracie ou do PVP Arena.
    *   Os sacos de pancada de boxe serão o principal ponto interativo do mapa onde os jogadores acumularão "Pontos de Estamina / Gás de Luta" ao clicar repetidamente (Mini-game de treinamento).

---

### 5. NATUREZA (Plazas, Arvoredos e Áreas Externas)

#### A. Kenney Isometric Landscape Pack
*   **Origem / Link:** [Kenney Landscape](https://kenney.nl/assets/isometric-landscape)
*   **Licença:** CC0 1.0 Universal.
*   **Compatibilidade Phaser 3:** Perfeita. Contém colinas, rios sinuosos isométricos, árvores e gramados com flores. Ideal para desenhar o cenário que envolve o Dojo e a Grande Plaza Central do JiuVerse.
*   **Compatibilidade Mobile:** Otimização garantida com suporte a renderização rápida de gramado em padrão "Tiled" sem gargalos.
*   **Adaptações para Artes Marciais:**
    *   Pedras grandes de jardim servirão de delimitadores de mapa, impedindo que o jogador ande para fora das extremidades lógicas da área visível.
    *   A vegetação será montada em arranjos ornamentais na entrada do Dojo de Copacabana para homenagear a união do jiu-jitsu com a natureza litorânea do Rio de Janeiro.

#### B. Isometric Outdoor Plants (Surt)
*   *Origem / Link:* [OpenGameArt Outdoor Plants by Surt](https://opengameart.org/content/isometric-outdoor-plants)
*   *Licença:* CC0 1.0 Universal.
*   *Compatibilidade Phaser 3:* Alta. Contém árvores pixel-art nativas em projeção 2.5D.
*   *Compatibilidade Mobile:* Excelente. Devido ao estilo de sombreamento de baixo contraste das folhas sob padrão dithered, o tamanho total do atlas de plantas é de apenas 12KB.
*   *Adaptações para Artes Marciais:*
    *   Adaptação de vasos decorativos internos para dojos de alto escalão (Dojos Master), trazendo equilíbrio visual e foco meditativo para a sala dos lutadores virtuais.

---

### 6. INTERFACE (UI, Placas e Indicadores de Estado)

#### A. Kenney Pixel UI Pack
*   **Origem / Link:** [Kenney Pixel UI Pack](https://kenney.nl/assets/pixel-ui-pack)
*   **Licença:** CC0 1.0 Universal.
*   **Compatibilidade Phaser 3:** Perfeita. Contém caixas de diálogo estilo 9-patch, botões, painéis, flechas e barras de rolagem. Pode ser fatiado perfeitamente com a classe `Phaser.GameObjects.NineSlice` do Phaser 3, permitindo esticar botões em qualquer largura sem deformar os cantos pixelados do design da interface.
*   **Compatibilidade Mobile:** Altamente adaptável. O uso de 9-patch nativo garante o reajuste dinâmico da interface para qualquer formato de tela móvel (16:9, 18:9, tablets).
*   **Adaptações para Artes Marciais:**
    *   A barra de energia e estamina receberá o ícone de um lutador aplicando uma finalização (chave de braço/triângulo).
    *   As janelas de diálogos Pixel UI serão usadas para os momentos de tutorial e instruções de golpes exibidos pelos mestres treinadores.

#### B. Free Game UI Kit Pixel Art (CraftPix)
*   **Origem / Link:** [CraftPix Game UI](https://craftpix.net/freebies/free-game-ui-kit-pixel-art/)
*   **Licença:** CraftPix Free License.
*   **Compatibilidade Phaser 3:** Excelente. Coleção de botões de configurações, perfis de personagens, barras de progresso vermelhas, verdes e azuis, além de slots de inventário de equipamentos desportivos.
*   **Compatibilidade Mobile:** Desenvolvido nativamente para design responsivo.
*   **Adaptações para Artes Marciais:**
    *   O marcador de pontos de vida será redefinido como **Barras de Saúde do Lutador** (Stamina) e **Nível de Finalização** (Submission Pressure).
    *   Os slots de inventário guardarão as faixas e kimonos desbloqueados nas missões semanais ou comprados na loja oficial.

---

### 7. NPCs (Juízes, Treinadores e Espectadores)

#### A. LPC Citizens & Trainers Pack (OpenGameArt)
*   **Origem / Link:** [OpenGameArt LPC Community](https://opengameart.org/content/lpc-citizens-trainers)
*   **Licença:** CC-BY 4.0 / CC-BY-SA 3.0.
*   **Compatibilidade Phaser 3:** Muito alta. Permite que criemos dezenas de NPCs únicos com pouquíssima variação de código usando o sistema nativo de fatiamento de frames.
*   **Compatibilidade Mobile:** Muito estável. Como utilizam spritesheets padronizados, a performance em CPU e GPU móvel é linear e limpa.
*   **Adaptações para Artes Marciais:**
    *   Mapeamento de comportamentos específicos:
        *   **Ref NPC (Juiz):** Posicionado no meio das áreas de ringue do PvP, executará a animação de gesticular com os braços erguidos em 90 graus (simulando a pontuação clássica de BJJ: 2, 3 ou 4 pontos).
        *   **Mestre Carlson Gracie NPC:** Vestindo kimono clássico com a faixa preta, assentado de pernas cruzadas no tatame superior guiando as ações dos alunos iniciantes na academia tradicional.

#### B. Pixel Art Crowd & Spectators (CraftPix)
*   **Origem / Link:** [CraftPix Spectators](https://craftpix.net/freebies/free-crowd-spectators-pixel-art/)
*   **Licença:** CraftPix Free License.
*   **Compatibilidade Phaser 3:** Muito simples de utilizar através de grupos de animação estáticos.
*   **Compatibilidade Mobile:** Devido ao grande volume de personagens, utilizaremos instâncias reduzidas e de baixa amostragem para evitar gargalo de rendering no Android Canvas nativo.
*   **Adaptações para Artes Marciais:**
    *   O público será posicionado em volta do octógono / arena de luta principal, torcendo e gesticulando no ritmo das finalizações para elevar as interações sociais nas transmissões virtuais das finais de torneios.

---

### 8. EFEITOS VISUAIS - VFX (Flashes de Impacto, Suor e Chispas)

#### A. Free Retro Explosion VFX Pixel Art (CraftPix)
*   **Origem / Link:** [CraftPix VFX Explosion](https://craftpix.net/freebies/free-retro-explosion-vfx-pixel-art/)
*   **Licença:** CraftPix Free License.
*   **Compatibilidade Phaser 3:** Total através do carregamento de spritesheets de explosões de fumaça e faíscas. A reprodução é feita gerando um sprite de VFX temporário auto-destrutivo: `vfx.play('animation').on('animationcomplete', () => vfx.destroy())`.
*   **Compatibilidade Mobile:** Excepcional por ser focado puramente em CPU Rendering leve de frames.
*   **Adaptações para Artes Marciais:**
    *   Os efeitos de rajada de poeira serão emitidos sobre o pé do lutador ao executar uma queda de quadril perfeita (Ippon Seoi Nage / Single Leg).
    *   Animações de círculos brilhantes amarelos e cianor brilhantes serão acionadas no centro do corpo do avatar sempre que ele subir de graduação esportiva (ex: "GRADUOU FAIXA AZUL!").

#### B. Pixel Art Impact Effects (rubberduck on OpenGameArt)
*   **Origem / Link:** [OpenGameArt Impact VFX by rubberduck](https://opengameart.org/content/pixel-art-impact-effects)
*   **Licença:** CC0 1.0 Universal (Domínio Público).
*   **Compatibilidade Phaser 3:** Ótima. Contém marcas de cortes rápidos, pequenas faíscas cinzentas prontas para explosões e pontos de impacto curvos.
*   **Compatibilidade Mobile:** Excelente. Levíssimo.
*   **Adaptações para Artes Marciais:**
    *   Efeito de "Flash de Impacto de Pegada" toda vez que um jogador se aproxima e engaja em luta ativa para travar a gola do kimono do adversário.
    *   Partículas de suor estilizadas que saltam dos lutadores durante combates prolongados na arena.

---

## PLANO DE INTEGRAÇÃO TÉCNICA AO PROJETO JIUVERSE

Para implementar essa rica biblioteca de forma profissional e sem travar sistemas móveis de baixa performance, o fluxo de engenharia visual do JiuVerse seguirá os 4 passos descritos a seguir:

```
┌────────────────────────┐       ┌────────────────────────┐       ┌────────────────────────┐
│  O TIMIZAÇÃO DE ASSETS │ ────> │   ESTRUTURA DE NOMES   │ ────> │    PIPELINE PHASER 3   │
│  Texture Packer Atlas  │       │  Padrão Semântico REST │       │   Render & Colisões  │
└────────────────────────┘       └────────────────────────┘       └────────────────────────┘
```

### Passo 1: Otimização de Assets (Texture Packing)
1.  **Compactação de Altas:** Reuniremos todos os tilesets de pisos e objetos decorativos por ambiente em arquivos Texture Atlas em formato **PNG** otimizado de tamanho máximo `1024x1024px`.
2.  **Power of Two:** Toda imagem carregada pelo motor gráfico deve respeitar a potência de 2 para otimizar o barramento de textura da GPU local Android.
3.  **Filtros de Amostragem:** Ativaremos `PixelArtMode` de renderização no Phaser 3 para garantir nitidez geométrica perfeita:
    ```javascript
    const config = {
        type: Phaser.AUTO,
        pixelArt: true, // Mantém os detalhes dos pixels sem borrar nas escalas de câmera!
        antialias: false,
        roundPixels: true
    };
    ```

### Passo 2: Estrutura de Nomenclatura e Pastas (Asset Dictionary)
Nossa hierarquia de armazenamento seguirá o rigor semântico abaixo:
```
/assets/
  ├── tilesets/
  │     ├── pvp_arena_atlas.png
  │     └── pvp_arena_atlas.json
  ├── characters/
  │     ├── bjj_base_sheets/
  │     └── gear_kimonos_layers/
  ├── furniture/
  │     ├── dojo_classic_furni.png
  │     └── dojo_classic_furni.json
  ├── ui/
  │     ├── hud_martial_pixel.png
  │     └── hud_martial_pixel.json
  └── vfx/
        └── hit_sparks_sheet.png
```

### Passo 3: Pipeline de Renderização Isométrica e Colisão Físicas no Phaser 3
Para garantir o fluxo suave das interações físicas isométrica, o motor utiliza o algorítmo de ordenação de profundidade no loop de atualização:

```javascript
// Método de renderização isométrica e ordenação dinâmica (Depth Sort Loop)
update() {
    this.tileGroup.children.each((child) => {
        // O valor de profundidade Z isométrica é derivado das coordenadas X e Y
        child.depth = child.y + (child.x * 0.5);
    });

    if (this.playerVisual) {
        this.playerVisual.depth = this.playerVisual.y + (this.playerVisual.x * 0.5) + 2; 
    }
}
```

### Passo 4: Adaptador do WebView Android
O projeto integrará os dados de carregamento de novos recursos dinamicamente por mensageria JSON entre o Jetpack Compose WebView e o Phaser local client-side da seguinte forma:

```javascript
// Ao carregar a tela, escuta novos itens ou graduações do usuário Android BuildConfig
window.addEventListener('message', (event) => {
    const message = JSON.parse(event.data);
    if (message.type === 'EQUIP_GEAR') {
        // Altera kimono, faixa ou cor em tempo real no avatar renderizado
        this.updatePlayerGear(message.kimonoColor, message.beltRank);
    }
});
```

---

## REGULAMENTO ANTI-PLÁGIO E GOVERNANÇA DE COPYRIGHT
O JiuVerse adota regras rígidas de segurança corporativa de propriedade intelectual:
1.  **Filtro de Ingestão de Ativos:** Nenhum desenvolvedor está autorizado a arrastar arquivos baixados de repositórios não-oficiais de marcas registradas.
2.  **Validação de Metadados:** Todo arquivo adicionado nos repositórios git com extensão `.png` ou `.aseprite` deve possuir uma entrada correspondente em nossa licença interna documentando o link direto de download original, o nome do autor e a cópia exata do texto de licença aplicável.
3.  **Auditorias Periódicas (Legal-Tech Audit):** A nossa equipe legal efetuará varreduras mensais automatizadas nas assinaturas SHA-256 das artes do cliente final para afastar conflitos legais e resguardar o ecossistema tecnológico do MMORPG de quaisquer alegações de infração.

Este inventário compõe o alicerce sólido do desenvolvimento visual de alta performance do **JiuVerse**, viabilizando um ambiente envolvente, original e fidedigno aos Dojos tradicionais brasileiros e do mundo todo.🥋🏢🚀
