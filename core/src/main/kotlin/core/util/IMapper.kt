package core.util

interface IMapper<T, U> {
    fun convertModelToDTO(model: T): U?
    fun convertDTOToModel(dto: U): T?
    fun convertDTOsToModels(dtos: List<U>): List<T>
    fun convertModelsToDTOs(models: List<T>): List<U>
}
