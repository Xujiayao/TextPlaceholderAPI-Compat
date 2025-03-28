<p align="center">
<img width=128 src="https://cdn.jsdelivr.net/gh/Xujiayao/TextPlaceholderAPI-Compat@master/icon.png">
</p>

# About Placeholder API Compat

Latest release of Patbox's Text Placeholder API, but compatible with all Fabric Minecraft versions 1.14.4+

This fork is primarily created for my multi-version mod, [Discord-MC-Chat](https://github.com/Xujiayao/Discord-MC-Chat).

## Usage

### For Users

Put the downloaded JAR file in the `mods` folder.

Notes:

- This mod functions as a library and will only operate when called by another mod.
- This mod can coexist with Patbox's official Placeholder API mod without conflicts, as each mod will call its respective library.

### For Developers

Ensure that your `mainProject` is set to the latest release version of Minecraft.

Only edit the `common.gradle` file. Remove `eu.pb4:placeholder-api` from the dependencies section.

Do NOT add the following to the wrapper's `build.gradle`, as it will break the library if used with versions other than the version specified in `mainProject`.

```groovy
repositories {
	maven {
		name = "Jitpack"
		url = "https://jitpack.io"
	}
}

dependencies {
	modCompileOnly("com.github.Xujiayao:TextPlaceholderAPI-Compat:2.6.1-compat.1")
}
```

Also edit all existing `fabric.mod.json` files.

```json
{
	"depends": {
		"fabricloader": ">=0.16.10",
		"java": ">=21",
		"placeholder-api-compat": "*"
	}
}
```

Finally, change the package name in your code from `eu.pb4.placeholders` to `com.xujiayao.placeholder_api_compat`.

## Support

If there is a bug or suggestion, or something you don't understand, you can [submit an issue](https://github.com/Xujiayao/TextPlaceholderAPI-Compat/issues/new/choose) on GitHub.

For compatibility issues, please submit your issues here. However, for issues related to the Placeholder API library itself, kindly direct your issue reports to the upstream GitHub repository. Thank you for your understanding!

Join my Discord server through: https://discord.gg/kbXkV6k2XU

## About Placeholder API

See [Patbox/TextPlaceholderAPI](https://github.com/Patbox/TextPlaceholderAPI)

It's a small, JIJ-able API that allows creation and parsing placeholders within strings and Minecraft Text Components.
Placeholder API uses a simple format of `%modid:type%` or `%modid:type data%` (`%modid:type/data%` prior to 1.19).
It also includes simple, general usage text format indented for simplifying user input in configs/chats/etc.

For information about usage (for developers and users) you can check official docs at https://placeholders.pb4.eu/!

\*[JIJ]: Jar-in-Jar

## License

This project is licensed under the [LGPL-3.0 license](https://github.com/Xujiayao/TextPlaceholderAPI-Compat/blob/master/LICENSE).