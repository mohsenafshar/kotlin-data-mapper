package feature.data.model

data class UserDTO(
    val firstName: String,
    val hobbies: List<HobbyDTO>,
    val address: AddressDTO,
)