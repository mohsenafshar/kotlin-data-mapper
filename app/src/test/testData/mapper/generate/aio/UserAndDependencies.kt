package feature.data.model

data class User(
    val firstName: String,
    val hobbies: List<Hobby>,
    val address: Address,
)

data class Hobby(
    val name: String,
)

data class Address(
    val street: String,
    val number: Int,
)