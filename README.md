# Kotlin DataMapper Plugin


[![Android Studio Plugin](https://img.shields.io/badge/plugin-AndroidStudio-green.svg)](https://plugins.jetbrains.com/plugin/25820-kotlin-data-mapper)
[![IntelliJ Idea Plugin](https://img.shields.io/badge/plugin-IntelliJ%20%20Idea-purple.svg)](https://plugins.jetbrains.com/plugin/25820-kotlin-data-mapper)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/25820-kotlin-data-mapper.svg)](https://plugins.jetbrains.com/plugin/25820-kotlin-data-mapper)
[![Version](https://img.shields.io/jetbrains/plugin/v/25820.svg?label=version)](https://plugins.jetbrains.com/plugin/25820-kotlin-data-mapper)

A powerful IntelliJ IDEA plugin that simplifies the process of generating mapping functions for Kotlin data classes. Designed to boost developer productivity by automating repetitive code generation tasks, particularly when converting between DTO and domain models.

---

## Features

- **Generate Mapping Functions**: Automatically generate mapping functions between two Kotlin data classes.
- **Support for Nested Classes**: Handles nested data classes recursively, ensuring accurate and clean mapping.
- **Customizable**: Configure mapping behavior through the settings panel.
- **Optimized for Kotlin**: Fully compatible with both K1 and K2 Kotlin compilers.
- **Simple Integration**: Easily accessible from the editor's context menu.

---

## Supported IDEs
- Android Studio — Jellyfish | 2023.3.1+
- IntelliJ IDEA Community — 2023.3+
- IntelliJ IDEA Ultimate — 2023.3+
- Aqua — 2024.1.1+

---

## Feedback

We want your feedback!

- Vote on [feature requests](https://github.com/mohsenafshar/kotlin-data-mapper/issues?q=is%3Aissue+is%3Aopen+label%3Afeature-request+sort%3Areactions-%2B1-desc). Votes help us drive prioritization of features
- [Request a new feature](https://github.com/mohsenafshar/kotlin-data-mapper/issues/new?labels=feature-request&template=feature_request.md)
- [Ask a question](https://github.com/mohsenafshar/kotlin-data-mapper/issues/new?labels=guidance&template=guidance_request.md)
- [File an issue](https://github.com/mohsenafshar/kotlin-data-mapper/issues/new?labels=bug&template=bug_report.md)
- Code contributions. See [our contributing guide](CONTRIBUTING.md) for how to get started.

---

## Getting Started

### Installation

1. Download the plugin from the [JetBrains Plugin Marketplace](https://plugins.jetbrains.com/plugin/25820-kotlin-data-mapper).
2. Install it directly in IntelliJ IDEA:
    - Open `File > Settings > Plugins`.
    - Search for "Kotlin Data Mapper".
    - Click "Install" and restart your IDE.

### Usage

1. Open any Kotlin file containing data classes.
2. Right-click inside the editor and select **Generate Mapping Function** from the context menu.
3. Select the source and target classes in the provided UI dialog.
4. The mapping function will be generated and inserted into your project.

---

## Configuration

The plugin provides a settings panel to customize its behavior:

1. Open `File > Settings > Tools > Kotlin Data Mapper Settings`.
2. Adjust the available options to suit your needs.
3. Save and apply your changes.

---

## Example

### Input

```kotlin
package ir.mohsenafshar.feature.data

data class UserDTO(
   val name: String,
   val fam: String,
   val age: Int,
   val address: AddressDTO
)

data class AddressDTO(
   val city: String,
   val apt: String,
   val streetInfo: StreetInfoDTO,
)

data class StreetInfoDTO(
   val name: String,
   val number: Int,
)
```

```kotlin
package ir.mohsenafshar.feature.domain

data class User(
   val age: Int,
   val fam: String,
   val address: Address
)

data class Address(
   val city: String,
   val apt: String,
   val streetInfo: StreetInfo,
)

data class StreetInfo(
   val name: String,
   val number: Int,
)
```

### Generated Output

```kotlin
fun UserDTO.toUser(): User {
   return User(
      age = this.age,
      fam = this.fam,
      address = Address(
         city = this.address.city,
         apt = this.address.apt,
         streetInfo = StreetInfo(name = this.address.streetInfo.name, number = this.address.streetInfo.number)
      )
   )
}
```

## Links

- **GitHub Repository:** [https://github.com/mohsenafshar/kotlin-data-mapper](https://github.com/mohsenafshar/kotlin-data-mapper)
- **Plugin on JetBrains Marketplace:** [https://plugins.jetbrains.com/plugin/25820-kotlin-data-mapper](https://plugins.jetbrains.com/plugin/25820-kotlin-data-mapper)

## Licensing

The plugin is distributed according to the terms outlined in our [LICENSE](LICENSE).