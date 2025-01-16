package feature.data.model

data class UserDTO(
    val firstName: String,
    val hobbies: List<HobbyDTO>,
    val address: AddressDTO,
)

data class HobbyDTO(
    val name: String,
)

data class AddressDTO(
    val street: String,
    val number: Int,
)