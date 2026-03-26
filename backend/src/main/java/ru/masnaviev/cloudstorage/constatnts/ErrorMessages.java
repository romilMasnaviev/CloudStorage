package ru.masnaviev.cloudstorage.constatnts;

public class ErrorMessages {
    //Security & Sessions
    public static final String ACCESS_DENIED = "Доступ запрещен";
    public static final String UNAUTHORIZED = "Пользователь не авторизован";

    //Validation
    public static final String USERNAME_MUST_NOT_BE_EMPTY = "Имя пользователя не должно быть пустым";
    public static final String USERNAME_LENGTH_BETWEEN_8_50 = "Длина имени пользователя должна быть от 8 до 50 символов";
    public static final String PASSWORD_MUST_NOT_BE_EMPTY = "Пароль не должен быть пустым";
    public static final String PASSWORD_LENGTH_BETWEEN_8_100 = "Длина пароля должна быть от 8 до 100 символов";
    public static final String PATH_MUST_NOT_BE_EMPTY = "Путь не должен быть пустым";
    public static final String USERID_MUST_NOT_BE_LESS_0 = "ID пользователя не может быть меньше 0";

    //Other
    public static final String USER_ALREADY_EXISTS = "Пользователь с таким именем уже существует";
    public static final String USERNAME_NOT_FOUND = "Пользователь не найден";

    //Minio
    public static final String MINIO_EXCEPTION = "Внутренняя ошибка хранилища";
    public static final String FILE_ALREADY_EXIST = "Файл уже существует";
    public static final String DIRECTORY_ALREADY_EXISTS = "Директория уже существует";
    public static final String PROTECTED_PARENT_DIRECTORY = "Родительская директория защищена и не может быть удалена";

    public static final String RESOURCE_NOT_FOUND = "Ресурс не найден";
    public static final String PATH_NOT_FOUND = "Путь не найден";
    public static final String DIRECTORY_NOT_FOUND = "Директория не найдена";
    public static final String FILE_NOT_FOUND = "Файл не найден";
    public static final String INVALID_OPERATION_COMBINATION = "Недопустимая комбинация операций";
    public static final String INVALID_RESOURCE_TYPE_CHANGE = "Недопустимое изменение типа ресурса (с файла на папку или наоборот)";

    public static final String PATH_MUST_BE_END_SLASH = "Путь должен оканчиваться символом '/'";
}
