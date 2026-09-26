# GovernixPrefix

Плагин для смены собственного префикса игроками с правом. Работает через LuckPerms meta — префикс сразу подхватывается чат-плагинами (GovernixChat, CMI, TAB).

## Возможности

- `/cprefix <текст>` — установить свой префикс
- `/cprefix reset` — сбросить префикс
- `/cprefix <ник> <текст>` — админ устанавливает другому
- `/cprefix reset <ник>` — админ сбрасывает
- **Проверки**: длина, blacklist, права на цвета/HEX/градиент/жирный
- **Кулдаун** между сменами (настраивается)
- Сохранение через **LuckPerms meta** → префикс виден во всех чат-плагинах

## Требования

- Paper 1.21.11 (или 1.21+)
- Java 21+
- **LuckPerms** (обязательно)

## Установка

1. Скачай `GovernixPrefix-1.0.0.jar` из Releases.
2. Положи в `plugins/`.
3. Перезапусти сервер.

## Команды

| Команда | Описание |
| --- | --- |
| `/cprefix <текст>` | Установить префикс |
| `/cprefix reset` | Сбросить префикс |
| `/cprefix <ник> <текст>` | Установить префикс другому |
| `/cprefix reset <ник>` | Сбросить префикс другому |

Алиасы: `/tag`, `/pref`, `/myprefix`, `/cpref`.

## Права

| Право | Описание | По умолчанию |
| --- | --- | --- |
| `governixprefix.use` | Смена своего префикса | **false** |
| `governixprefix.color` | `&a`-цвета в префиксе | false |
| `governixprefix.hex` | `&#RRGGBB` HEX-цвета | false |
| `governixprefix.gradient` | `&gradient[#X;#Y;текст]` | false |
| `governixprefix.bold` | `&l` жирный | false |
| `governixprefix.admin` | Смена другим игрокам | op |
| `governixprefix.cooldown.bypass` | Обход кулдауна | op |

## Использование

### 1. Создай группу в LuckPerms

```
/lp creategroup legend
/lp group legend setweight 150
/lp group legend meta setprefix "&6&l[LEGEND] "
```

### 2. Выдай права группе

```
/lp group legend permission set governixprefix.use true
/lp group legend permission set governixprefix.color true
/lp group legend permission set governixprefix.hex true
/lp group legend permission set governixprefix.bold true
```

### 3. Добавь игрока в группу

```
/lp user doniel_darov parent add legend
```

### 4. Игрок меняет префикс

```
/cprefix &6&l[MOJ] 
```

или с HEX:

```
/cprefix &#FF00AA&#00FFAA[FIRE] 
```

### 5. Проверь

```
/lp user doniel_darov meta info
```

Должен быть `prefix.100.&6&l[MOJ]`.

## Конфиг

```yaml
prefix-priority: 100
min-length: 2
max-length: 24
cooldown-seconds: 120

blacklist:
  - "admin"
  - "owner"

restrictions:
  max-decorations: 3
  allow-empty-text: false
```

## Как работает с чатом

Префикс сохраняется в **LuckPerms meta**. Чат-плагины (GovernixChat, CMI, TAB) читают его через PlaceholderAPI `%luckperms_prefix%`.

**В GovernixChat** — ничего настраивать не надо, уже работает через `buildNickname()`.

**В CMI** — если у тебя CMI показывает префикс, он тоже подхватит автоматически.

## Сборка

```bash
git clone https://github.com/ТВОЙ_НИК/GovernixPrefix.git
cd GovernixPrefix
mvn clean package
```

## Лицензия

MIT.
