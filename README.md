# LabyController

Full controller support for Minecraft via LabyMod 4. Play with your gamepad!

Inspired by [Controlify](https://github.com/isXander/Controlify) by isXander.

## Supported Versions

1.19.4, 1.20.1, 1.20.2, 1.20.4, 1.20.6, 1.21.1, 1.21.3, 1.21.4, 1.21.5, 1.21.8

## Features

- **Full Gamepad Support** - Xbox, PlayStation, Switch Pro, and generic controllers
- **SDL3 Integration** - Native controller support with proper button detection
- **Customizable Controls** - Rebind any button to any action
- **Radial Menu** - Quick action wheel for hotbar slots and more
- **On-Screen Keyboard** - Type in chat using your controller
- **Vibration Feedback** - Haptic feedback for damage, attacks, mining
- **Light Bar Control** - Dynamic light bar colors on PS/Switch controllers
- **GUI Navigation** - Navigate menus with your controller

## Supported Controllers

| Controller | Support Level |
|------------|---------------|
| Xbox One/Series | Full |
| PlayStation 4/5 | Full (incl. touchpad, light bar) |
| Nintendo Switch Pro | Full |
| Steam Deck | Full |
| Generic XInput | Full |

## Installation

1. Download the latest release from [Releases](../../releases)
2. Place the JAR in your LabyMod addons folder
3. Launch LabyMod and connect your controller

## Default Controls

| Action | Button |
|--------|--------|
| Jump | A / Cross |
| Sprint | L3 |
| Sneak | R3 |
| Attack | RT / R2 |
| Use | LT / L2 |
| Inventory | Y / Triangle |
| Drop Item | D-Pad Down |
| Open Chat | D-Pad Up |
| Radial Menu | D-Pad Right |
| Pick Block | D-Pad Left |
| Hotbar Next | RB / R1 |
| Hotbar Prev | LB / L1 |
| Pause | Start / Options |

## Building

```bash
./gradlew build
```

Output JAR is in `build/libs/`.

## Credits

- [Controlify](https://github.com/isXander/Controlify) by isXander - Original Fabric mod that inspired this addon
- [SDL3](https://libsdl.org/) - Cross-platform controller support
- [libsdl4j](https://github.com/isXander/libsdl4j) - SDL3 Java bindings by isXander
- [LabyMod](https://labymod.net/) - Minecraft client platform

## License

This project is open source. See LICENSE for details.
