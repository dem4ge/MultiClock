# Контекст проекта MultipleChessClok

> **Поддержка:** при существенных изменениях архитектуры, фич или известных проблем — обновляйте этот файл.

## Назначение

Многопользовательский шахматный таймер (2–6 игроков, по умолчанию 4): обратный отсчёт, один активный таймер, смена хода по правилам, пауза, настройки, переупорядочивание карточек (очерёдность по часовой стрелке).

## Стек

- Kotlin, Jetpack Compose, Material 3  
- **minSdk 26**, compile/target **35**  
- Тик таймера: **kotlinx.coroutines** (`delay`), без `Handler`  
- Пакет: `com.multiplechessclok.app`

## Архитектура

Слоистая схема с **MVVM** и одним источником правды во **ViewModel**:

| Слой | Пакет / расположение | Роль |
|------|----------------------|------|
| **UI** | `ui.*`, `ui.components`, `ui.theme` | Compose-экраны, тема, строки времени, сетка, карточки, нижняя панель |
| **Presentation** | `presentation` | `ChessClockViewModel`, `ChessClockUiState`, маппинг domain → UI |
| **Domain** | `domain`, `domain.model` | Правила (`ChessClockRules`), чистые переходы состояния (`ChessClockReducer`), тик (`ChessClockTimerEngine`), события/модели |
| **Data** | `data` | Дефолты старта (`GameConfigurationDefaults`), опирается на правила domain |

**Поток данных:** пользовательские действия → ViewModel → `ChessClockReducer.reduce` / тик → обновление `domainState` → `domainToUi` → `StateFlow<ChessClockUiState>`.

## Ключевые классы и файлы

### Domain

- **`ChessClockRules`** — константы (число игроков 2–6, длительности, шаг тика и т.д.).
- **`ChessClockReducer`** — чистая логика: `TapPlayer`, `Pause`, `Reorder` / `ReorderByPlayerIds`, `ApplySettings`, `Reset`, `Tick`.
- **`ChessClockTimerEngine`** — обёртка над одним шагом `Tick` (изоляция тика от UI).
- **`ChessClockEvent`** (sealed), **`ChessClockDomainState`**, **`PlayerSlot`**, **`PlayerId`** (value class).

### Presentation

- **`ChessClockViewModel`** — `applyDomain`, корутинный цикл тика, `onTapPlayer`, `onPause`, `onReorder` (индексы), **`onReorderByPlayerIds`**, настройки/лист.
- **`ChessClockUiState`**, **`PlayerCardUiState`**, **`SettingsUiState`**.

### UI

- **`MainActivity`**, **`ChessClockApp`** — корень, `ModalBottomSheet` настроек, `WindowInsets.safeDrawing`.
- **`LandscapeAlignedPlayerGridHost`** — сетка «как в ландшафте»; в портрете — поворот контейнера (`graphicsLayer rotationZ = -90°`).
- **`PlayerGrid`** — сетка 2×N, **long-press + `drag`**, `awaitLongPressOrCancellation` (тап vs перетаскивание), `zIndex` + `graphicsLayer` смещение, live-reorder по пересечению bounds в root, колбэк **`onReorderByPlayerIds`**.
- **`PlayerCard`** — только отображение (жесты на родительском `Box`).
- **`ChessClockBottomBar`** — пауза и настройки, низ экрана.
- **`SettingsContent`** — время (минуты), число игроков, имена; цвета из `MaterialTheme`.
- **`TimeFormatting.formatHhMmSs`** — отображение **HH:MM:SS**.
- **`ChessClockTheme`**, **`PlayerColors`** — приглушённая палитра.

### Ресурсы

- Адаптивная иконка: `mipmap-anydpi-v26`, `drawable/ic_launcher_foreground|background`, PNG в `mipmap-*dpi`, исходник 1024 — `drawable-nodpi/ic_launcher_source.png`.

## Основные фичи

- Сетка 2 колонки (при 2 игроках — 2 в ряд); динамическое число строк.
- Тап: первый тап запускает игрока; тап активного (не на паузе) — передать ход следующему по **текущему** порядку списка; тап другого игрока при активном — игнор; тап активного на паузе — возобновление.
- Пауза из нижней панели; сброс и настройки из шита.
- Настройки: общее время, имена, 2–6 игроков, сброс таймеров.
- Перетаскивание: long-press, визуальное смещение, **`ReorderByPlayerIds`** в domain, порядок списка = порядок «по часовой стрелке».

## Договорённости

- **Именование:** префикс **`ChessClock*`** у домена/VM/UI-имён приложения (исторически); продукт и Gradle **`MultipleChessClok`** (намеренное написание в названии приложения).
- **Состояние UI:** неизменяемые data class + подъём состояния там, где нужно; единый `StateFlow` из ViewModel.
- **События:** предпочтительно sealed `ChessClockEvent`; reorder по UI — стабильные **`player.id`**, не только видимый индекс.
- **Стиль UI:** Material 3, приглушённые цвета, без «кислотных» оттенков; настройки — цвета из `colorScheme`, не хардкод.
- **Числа:** длительности и шаги тика через **`ChessClockRules`** / константы, не «магические числа» в бизнес-логике (UI-допуски типа отступов — локальные `private val`).
- **Тесты:** JVM — reducer, формат времени, ViewModel; Android — `PlayerGridReorderUiTest` (long-press + drag).

## Тесты

- `app/src/test/...` — unit.
- `app/src/androidTest/...` — инструментальные (нужен эмулятор/девайс для полного прогона).

## Известные проблемы и ограничения

1. **`local.properties`** с `sdk.dir` не коммитить как общий артефакт — привязка к машине.
2. **Инструментальный тест reorder** зависит от размера ячеек/DPI; жест `moveBy(Offset(...))` может потребовать подстройки на узких экранах.
3. **Портрет + поворот сетки:** hit-testing после `graphicsLayer` рассчитан на `boundsInRoot` / `localToRoot` с той же ячейкой, где висит `pointerInput`; при смене логики поворота проверять drag заново.
4. **Live-reorder при перетаскивании:** повторные вызовы приходят только при смене «наведённого» `playerId`; возможны пограничные случаи на границах ячеек и щелях между карточками.
5. Название **`MultipleChessClok`** (без второй «c» в Clock) зафиксировано как имя продукта; расхождение с `ChessClock*` в коде осознанное.

## История правок документа

| Дата | Изменение |
|------|-----------|
| 2026-03-28 | Первоначальное заполнение по текущему дереву проекта и обсуждению в чате. |
