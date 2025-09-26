# CodeCraft
![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-green)
![Forge](https://img.shields.io/badge/Forge-47.3.0-orange)
![Java](https://img.shields.io/badge/Java-17-blue)

Мод для Minecraft 1.20.1 Forge, добавляющий встроенный JavaScript компилятор с графическим интерфейсом.

## Возможности

- 📝 Встроенный редактор скриптов с подсветкой синтаксиса
- ⚡ Выполнение JavaScript кода в реальном времени
- 🎮 API в стиле Mappet для взаимодействия с игрой
- 💾 Сохранение скриптов в мире Minecraft
- ⌨️ Горячие клавиши и команды для управления

## Установка

1. Скачайте последнюю версию мода из [Releases](https://github.com/pirozhok/CodeCraft/releases)
2. Поместите файл `.jar` в папку `mods`
3. Запустите игру с установленным Forge
   P.s. мод написан на Forge 47.3.0 и тестировался на этой версии.

## Использование

### Графический интерфейс
- Нажмите `=` для открытия редактора скриптов (клавишу открыитя gui можно изменить в настройках)
- Создавайте, редактируйте и запускайте скрипты через GUI

### Команды
- `/script list` - список всех скриптов
- `/script exec <name>` - запустить скрипт
- `/script stop <name>` - остановить скрипт
- `/script reload <name>` - перезагрузить скрипт

### Пример скрипта
```javascript
function main(c) 
{
    c.tellrawGold("Hello world!");
    c.command("/give @a minecraft:diamond 1");
}
```

### Другое
- С написанием кода помогал DeepSeek,
- Это только прототип мода, пока еще делаю улучшения,
- По всем вопросам - мой дискорд _pirozhok_off
