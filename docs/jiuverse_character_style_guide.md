# JIUVERSE: GUIA INTEGRAL DE DIREÇÃO DE ARTE E PRODUÇÃO DE SPRITES (HD PIXEL ART) 🥋👤
### Manual Corporativo de Engenharia Visual de Avatares, Vestimentas e Equipamentos Isométricos

**Autores:** Diretor de Arte AAA, Lead Character Designer, UX Designer e Arquiteto de Assets 2.5D.
**Data:** 6 de Junho de 2026.
**Escopo do Documento:** Definição completa do padrão anatômico, proporções masculinas/femininas, mapeamento de equipamentos esportivos (kimonos, faixas, acessórios) e modelo de coordenadas para produção de spritesheets HD em projeção isométrica dimétrica (2:1).

---

## SEÇÃO 1: DIRETRIZES DA ANÁLISE ESTÉTICA DE REFERÊNCIAS VS. JIUVERSE DEFINITIVO

Nosso benchmark examinou minuciosamente as técnicas de animação e renderização histórica de mundos virtuais do início de 2000 (como visto em `Habbo Assets`, `Origins Furni` e `Habborator Art`). Identificamos limitações fundamentais e definimos como o **JiuVerse** se sobressai em originalidade e sofisticação no mercado de 2026:

### 1.1 Limitações Históricas Encontradas (The Heritage Constraints)
*   **Aliasing de Baixa Resolução:** Sprites clássicos de 32x64px sofrem com "Pixelation Effect" em telas Retina/AMOLED de densidades de até 500ppi, resultando em imagens borradas ou cansativas.
*   **Anatomia Genérica:** Modelos de corpo idênticos onde apenas a paleta de cores ou cabelo sutilmente mudava, prejudicando a representação realista dos atletas.
*   **Pouco Contraste nos Detalhes Esportivos:** A dobra de um kimono de BJJ grosso ou o nós de uma amarração de faixa de graduação se perdiam em resoluções minúsculas de spritesheets tradicionais.

### 1.2 O Salto Tecnológico JiuVerse (HD Pixel Art Vetorial)
O JiuVerse adota a **Pixel Art HD (DDP - Double Density Pixel-Art)** integrada à renderização vetorial no Canvas. Cada avatar é dimensionado em resolução nominal basal de **$64 \times 96\text{ px}$** por frame direcionado, permitindo:

*   **Pixel-Perfect Antialiasing:** Borda exterior preta de $1\text{ px}$ suavizada internamente por gradientes de sub-pixels, mantendo a nitidez perfeita.
*   **Fidelidade Somatotipo:** Estruturas físicas parrudas que expressam a anatomia clássica de lutadores (pescoço largo, ombros fortes, postura de guarda).
*   **Dynamic Customization Mapping:** Sprites recortados matematicamente em fatias e sobrepostos dinamicamente com cálculo em tempo de execução via matriz `LayerPriority` no Compose e no Phaser 3.

---

## SEÇÃO 2: PROPORÇÕES E ANATOMIA DOS PERSONAGENS (THE BASE BASES)

Diferente de moldes Chibi tradicionais extremamente cartunescos (cabeça gigante 1:2), o JiuVerse estabelece a **Proporção Esportiva Proporcional (3.5 H - 3.5 Cabeças)**, conferindo atitude atlética e possibilitando a perfeita visualização dos golpes e transições no solo.

```
       Visualização Frontal da Anatomia Base (3.5 Cabeças de Altura)
       
       [ CABEÇA: 1.0H ]    ╭─────────╮     -> Cabeça estilizada, testa proeminente
                           │  (o.o)  │        para leitura ágil das feições faciais.
       [ TRONCO: 1.2H ]   ╭┴─────────┴╮    -> Ombros estruturados, pescoço largo,
                          │ / 🥋   \ │        peitoral projetado em guarda defensiva.
       [ QUADRIL: 0.6H ]  │ \ Faixa / │    -> Cintura firme ideal para as amarrações.
                          ╰──┬─────┬──╯
       [ PERNAS: 0.7H ]      │  │  │       -> Pernas compactadas, panturrilhas densas,
                             🦶    🦶         base firme para o equilíbrio no tatame.
```

### 2.1 Padrão Anatomia Masculina (The Heavy Guard Stance)
*   **Largura Basal do Ombro:** $22\text{ px}$ (Projetando silhueta de triângulo invertido sutil).
*   **Pescoço:** Trapézio encorpado e largo de $6\text{ px}$ para denotar força de estamina contra chaves de pescoço.
*   **Mãos:** Punhos fechados cilíndricos prontos para pegadas firmes nas golas dos kimonos do oponente.
*   **Ângulo de Repouso (Idle Frame 1):** Braços ligeiramente abertos perpendiculares à cintura com os joelhos semicurvados para amortecimento em quedas.

### 2.2 Padrão Anatomia Feminina (The Agile Passer Stance)
*   **Largura Basal do Ombro:** $18\text{ px}$ (Estrutura ágil e de alta mobilidade).
*   **Quadril:** Levemente acentuado nas laterais para garantir que a dobra do Kimono assente de forma perfeita sem escorregar visualmente.
*   **Pescoço:** Linha elegante e definida de $4\text{ px}$ integrada com contorno marcado.
*   **Postura Geral:** Joelhos retos aproximados no centro da célula, elevando o ponto de gravidade e refletindo velocidade nas passagens de guarda.

---

## SEÇÃO 3: ESPECIFICAÇÃO DE EQUIPAMENTOS E ATIVOS ESPORTIVOS (ORIGINAL GEAR)

Cada item de armário e vestuário da biblioteca JiuVerse segue um esquema numérico de cores, sombreamento dinâmico e posicionamento preciso nas coordenadas locais do corpo base.

```
                      Corte Superior de Bonés e Cabelos
                                   /\
                         [ BONE ] /  \ [ CABELO ]
                                 /────\
                                ╭│(o_o)│╮  <- Altura Z: 72 a 92px
                                 \────/
                             [ GOLA KIMONO ] -> Altura Z: 48 to 60px
                                   /\
                   [ MANGA ] <─── /  \ ───> [ MANGA ]
                                 /────\
                              [ FAIXA BJJ ]  <- Altura Z: 32 to 44px
                                 \────/
                                 /│  │\
                                🦶    🦶     <- Altura Z: 0 to 12px
```

---

### 3.1 Faixas de Graduação Oficial de BJJ (`belts_bjj_sheet`)
As faixas representam o respeito e o progresso espiritual de cada atleta do JiuVerse. Elas envolvem o ponto médio do tórax ($Y = 36\text{ px}$ nas coordenadas locais do sprite).

*   **Paleta de Cores de Competição Real:**
    1.  *Faixa Branca (Iniciante):* `Branco Alabastro (#F8FAFC)` com ponteira preta.
    2.  *Faixa Azul (Graduado):* `Azul Combate (#2563EB)` com ponteira preta e listras brancas.
    3.  *Faixa Roxa (Avançado):* `Roxo Nobre (#7C3AED)` com ponteira preta.
    4.  *Faixa Marrom (Elite):* `Marrom Terra (#78350F)` com ponteira preta.
    5.  *Faixa Preta (Mestre):* `Preto Carbono (#0F172A)` com ponteira vermelha deslumbrante e pontas de listras douradas.
*   **Física de Sombreamento:** Borda preta de realce e uma listra paralela central de $1\text{ px}$ cinza que confere curvatura e volumetria de amarração em nó cego real.

### 3.2 Kimonos de Combate Nativos (`kimono_suits_sheet`)
O kimono de BJJ é largo, feito de algodão trançado denso. É desenhado com linhas inclinadas de $120^{\circ}$ nas lapelas sobrepostas.

*   **Moldes Especiais:**
    *   `KIMONO_WHITE` (Branco tradicional): Gola com sombras azuis e punhos marcados.
    *   `KIMONO_ROYAL` (Azul oficial CBJJ): `Azul Royal (#1E40AF)` com costuras contrastantes douradas ou laranjas ao longo de toda a manga.
    *   `KIMONO_BLACK` (Preto ninja): `Preto Grafite (#1A202C)` com detalhes de lapela vermelhos agressivos.
    *   `KIMONO_GOLDEN` (Edição Lendária de Campeão): Tecido em `Champanhe Fosco (#FEF08A)` com acabamentos dourados brilhantes cintilantes.

### 3.3 Jaquetas de Equipe e Agasalhos Urbanos (`jackets_street_sheet`)
Roupas de frio para circulação casual na Praça Central ou nos Escritórios de Clãs.

*   **Especificações Técnicas:**
    *   Modelagem fechada até o queixo via zíper vertical metálico prata de $1\text{ px}$.
    *   Silhueta larga nas mangas que cobre levemente as mãos do lutador para proteção e estilo urbano de 2026.
    *   *Símbolo de Guilda:* O verso da jaqueta possui um canal com máscara alfa dinâmica estruturado para carregar o brasão do clã atualizado automaticamente.

### 3.4 Bonés, Chapéus e Toucas (`headwear_gear_sheet`)
Montados sobre a camada de cabelo do personagem, ancorados acima de $Y = 72\text{ px}$ a partir do pé do lutador.

*   **Padrões Geométricos:**
    *   *Boné Aba Reta:* Aba inclinada no ângulo isométrico de exatos $26.5^{\circ}$ apontando para frente-esquerda ou frente-direita (dependendo da direção do olhar do avatar).
    *   *Touca de Lã Zen:* Cobre totalmente as orelhas do avatar base, com textura tricotada quadriculada de pixels de contraste alternados em damier de $1\text{ px}$.

### 3.5 Mochilas e Sacas de Viagem (`backpacks_equip_sheet`)
Equipamento traseiro carregado nas costas, visível principalmente nas posições de perfil e costas (Norte, Leste, Oeste).

*   **Modelos de Engenharia Visual:**
    *   *Mochila de Treino Tática (Roll-top):* Bolsa cilíndrica de náilon impermeabilizado com fivelas cruzadas horizontais e suportes externos de garrafas de suplemento de hidratação.
    *   *Saco Kimoneiro Transversal:* Alça larga que cruza o peitoral em diagonal de $45^{\circ}$ de forma perfeita, sem colidir com as dobras de lapela do kimono base.

### 3.6 Medalhas e Condecorações de Torneio (`medals_honor_sheet`)
Sinalizadores de vitória exibidos pendurados horizontalmente no centro do peito do lutador.

*   **Graus de Prestígio:**
    *   *Medalha de Ouro de Grand Slam:* Metal amarelado brilhante com fita azul-royal que oscila suavemente com $1\text{ px}$ de deslocamento vertical harmônico senoidal quando o avatar respira no tatame.

---

## SEÇÃO 4: MATRIZ DE ROTAÇÃO E PERSPECTIVA ISOMÉTRICA (8 DIREÇÕES)

Para cada frame de animação, o artista de character design de 2D deve produzir a rotação e o desenho nos 8 eixos cardeais tradicionais do motor de renderização JiuVerse.

```
                            NORTE (0)
                        O0 ↖   ▲   ↗ O1
                            \  │  /
           OESTE (6)  ◄─────┴──┼──┴─────► LESTE (2)
                            /  │  \
                        O2 ↙   ▼   ↘ O3
                            SUL (4)
```

### Código Identificador de Ângulos (Mapping Enum)
*   **DIR_0 (NORTE):** Costas reta do avatar. Ideal para ver detalhes de patch de clã nas costas.
*   **DIR_1 (NORDESTE):** Perfil direito de costas.
*   **DIR_2 (LESTE):** Perfil direito total de lutador. Jaquetas e bolsas à mostra.
*   **DIR_3 (SUDESTE):** Frente inclinada direita. **Ângulo padrão da câmera de lobby e conversas.**
*   **DIR_4 (SUL):** Frente reta com rosto de alta leitura.
*   **DIR_5 (SUDOESTE):** Frente inclinada esquerda.
*   **DIR_6 (OESTE):** Perfil esquerdo total.
*   **DIR_7 (NORDESTE):** Perfil esquerdo de costas.

---

## SEÇÃO 5: GUIA E PLANILHA DE PRODUÇÃO DE SPRITES (THE SPRITE SHEET BIBLE)

A fim de assegurar fluxo de exportação eficiente para o desenvolvedor e carregamento sem gargalos de CPU no mobile, cada Spritesheet de Categoria de Roupa ou Base de Avatar deve seguir o seguinte esquema dimensional rígido de grade e farias de carregamento físico:

```
    ┌───────┬───────┬───────┬───────┬───────┬───────┬───────┬───────┐
    │ DIR0  │ DIR1  │ DIR2  │ DIR3  │ DIR4  │ DIR5  │ DIR6  │ DIR7  │ -> Linha 0: IDLE (Frame 1)
    │ 64x96 │ 64x96 │ 64x96 │ 64x96 │ 64x96 │ 64x96 │ 64x96 │ 64x96 │
    ├───────┼───────┼───────┼───────┼───────┼───────┼───────┼───────┤
    │ DIR0  │ DIR1  │ DIR2  │ DIR3  │ DIR4  │ DIR5  │ DIR6  │ DIR7  │ -> Linha 1: WALK (Frame 1)
    ├───────┼───────┼───────┼───────┼───────┼───────┼───────┼───────┤
    │ DIR0  │ DIR1  │ DIR2  │ DIR3  │ DIR4  │ DIR5  │ DIR6  │ DIR7  │ -> Linha 2: WALK (Frame 2)
    ├───────┼───────┼───────┼───────┼───────┼───────┼───────┼───────┤
    │ DIR0  │ DIR1  │ DIR2  │ DIR3  │ DIR4  │ DIR5  │ DIR6  │ DIR7  │ -> Linha 3: COMBATE/GUARDA
    └───────┴───────┴───────┴───────┴───────┴───────┴───────┴───────┘
```

### 5.1 Parâmetros Dimensionais do Arquivo PNG de Spritesheet
*   **Largura da célula:** Exatos $64\text{ px}$.
*   **Altura da célula:** Exatos $96\text{ px}$.
*   **Margem interna (Padding):** $0\text{ px}$ (A imagem deve estar alinhada com as bordas tangentes da célula).
*   **Fila Horizontal (X):** Direções de $0$ a $7$. Totalizando $64\text{ px} \times 8 = 512\text{ px}$ de largura de imagem final da folha de sprite.
*   **Linha Vertical (Y):** Estados de Animações consecutivas (IDLE, WALK1, WALK2, WALK3, WALK4, SITTING, COMBATE, SALUTAÇÃO).
*   **Filtro de Amostragem do Motor:** `Nearest-Neighbor (Sem Antialiasing Bicúbico)` para garantir o visual pixel art cristalino de altíssima fidelidade sem embaçamento.

### 5.2 Estrutura de Camadas sobrepostas de Desenho (Z-Rendering Layering Priority)
Durante o ciclo de pintura frame-a-frame no Canvas nativo ou na renderização do Phaser 3, os elementos fatiados empilham-se obedecendo a ordem de desenho de baixo para cima:

1.  `LAYER_0 (SOMBRA SOLO):` Elipse preta translúcida com $30\%$ de opacidade ancorada no chão do Tatame.
2.  `LAYER_1 (BASE ANATOMICA):` Corpo nu masculino ou feminino (cor de pele RGB parametrizada).
3.  `LAYER_2 (CABELO/PENTEADO):` Penteados de lutadores (coques baixos, cabelos curtos raspados ou amarrados).
4.  `LAYER_3 (KIMONO_CALCA):` Calça do Kimono envolvendo quadris e pernas do lutador.
5.  `LAYER_4 (KIMONO_JAQUETA):` Jaqueta de Kimono aberta ou fechada sobreposta aos ombros do avatar.
6.  `LAYER_5 (FAIXA_BJJ):` Amarração de faixa central que abraça e amarra a Calça e a Jaqueta.
7.  `LAYER_6 (ACC_BAG):` Mochilas de ginásio ou garrafas transversais traseiras.
8.  `LAYER_7 (ACC_HEAD):` Bonés, óculos esportivos ou cicatriz de luta por cima da cobertura facial.
9.  `LAYER_8 (ACC_HANDS):` Luvas de sparring sem dedos ou relógio esportivo de medição de batimentos cardíacos.

---

## FÓRMULA DE CARREGAMENTO AUTOMÁTICO EM CÓDIGO (JSON DE METADADOS)

Este gabarito simplificado de dados reativos é incorporado aos empacotadores Phaser 3 e ao sistema de carregamento assíncrono Android do JiuVerse para montar os atletas de forma perfeita:

```json
{
  "avatarCollection": "JiuVerse_S1_2026",
  "baseProperties": {
    "cellWidth": 64,
    "cellHeight": 96,
    "pixelRatio": "HD_DDP"
  },
  "somatotypes": {
    "male_heavy": {
      "baseSprite": "assets/sprites/char_base_male_hd.png",
      "shoulder_offset_px": 3,
      "head_anchor_y": 76
    },
    "female_passer": {
      "baseSprite": "assets/sprites/char_base_female_hd.png",
      "shoulder_offset_px": 0,
      "head_anchor_y": 74
    }
  },
  "states": {
    "idle": { "rowY": 0, "framesCount": 4, "animationFps": 6 },
    "walk": { "rowY": 1, "framesCount": 6, "animationFps": 12 },
    "combat_stance": { "rowY": 2, "framesCount": 1, "animationFps": 0 },
    "bow_respect": { "rowY": 3, "framesCount": 3, "animationFps": 8 }
  }
}
```

---

## CONCLUSÃO E DIRETRIZES FINAIS DO COMITÊ DE ARTE

Com este manual de proporções refinadas, anatomia do Brazilian Jiu-Jitsu bem fundamentada, tabela de fardamentos esportivos e especificações dimensionais de alto detalhamento das células de grade dimétrica (2:1), o **JiuVerse** se consolida como um produto com independência artística integral e as melhores garantias contra violações de patentes de visual de marcas concorrentes de meados dos anos 2000.

O time de desenho e animação de personagens está totalmente guarnecido para entregar com excelência o produto mais impressionante e autêntico de 2026! **OSS!** 🥋🇧🇷
