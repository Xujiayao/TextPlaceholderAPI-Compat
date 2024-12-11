<p align="center">
<img width=128 src="https://cdn.jsdelivr.net/gh/Xujiayao/TextPlaceholderAPI-Compat@master/icon.png">
</p>

# About Placeholder API Compat

Latest release of Patbox's Text Placeholder API, but compatible with all Fabric Minecraft versions 1.14.4+

This fork is primarily created for my multi-version mod, [Discord-MC-Chat](https://github.com/Xujiayao/Discord-MC-Chat).

## Usage (For Developers)

```groovy
// Gradle

repositories {
	maven {
		name = "Jitpack"
		url = "https://jitpack.io"
	}
}

dependencies {
	modImplementation("com.github.Xujiayao:TextPlaceholderAPI-Compat:2.5.1-compat.1")
}
```

## Support

If there is a bug or suggestion, or something you don't understand, you can [submit an issue](https://github.com/Xujiayao/TextPlaceholderAPI-Compat/issues/new/choose) on GitHub.

For compatibility issues, please submit your issues here. However, for issues related to the Placeholder API library itself, kindly direct your issue reports to the upstream GitHub repository. Thank you for your understanding!

Join my Discord server through: https://discord.gg/kbXkV6k2XU

## About Placeholder API

See [Patbox/TextPlaceholderAPI](https://github.com/Patbox/TextPlaceholderAPI)

It's a small, jij-able API that allows creation and parsing placeholders within strings and Minecraft Text Components.
Placeholders use simple format of `%modid:type%` or `%modid:type/data%`.
It also includes simple, general usage text format indented for simplifying user input in configs/chats/etc.

For information about usage (for developers and users) you can check official docs at https://placeholders.pb4.eu/!

## License

This project is licensed under the [LGPL-3.0 license](https://github.com/Xujiayao/TextPlaceholderAPI-Compat/blob/master/LICENSE).