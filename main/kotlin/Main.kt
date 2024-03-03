import java.util.Scanner

// Базовый класс пользователя
open class User(val username: String, var password: String)

// Класс администратора
class Administrator(username: String, password: String) : User(username, password) {
    private val users: MutableMap<String, User> = mutableMapOf()

    fun addUser(newUser: User) {
        users[newUser.username] = newUser
    }

    fun removeUser(username: String) {
        users.remove(username)
    }

    fun editUserPassword(username: String, newPassword: String) {
        val user = users[username]
        user?.let {
            user.password = newPassword
        }
    }

    fun displayUsers() {
        println("Registered Users:")
        for ((username, _) in users) {
            println(username)
        }
    }
}

// Класс гостя
class Guest(username: String, password: String) : User(username, password) {
    fun viewItems(store: ElectronicsStore) {
        store.displayInventory()
    }
}

// Класс складовщика
class Storekeeper(username: String, password: String) : User(username, password) {
    fun checkStock(store: ElectronicsStore, item: String): Int {
        return store.getQuantity(item)
    }

    fun addStock(store: ElectronicsStore, item: String, quantity: Int) {
        store.addItem(item, quantity)
    }
}

// Класс магазина электроники
class ElectronicsStore {
    private val inventory: MutableMap<String, Int> = mutableMapOf()

    fun addItem(item: String, quantity: Int) {
        inventory[item] = inventory.getOrDefault(item, 0) + quantity
    }

    fun removeItem(item: String, quantity: Int) {
        val currentQuantity = inventory.getOrDefault(item, 0)
        if (currentQuantity >= quantity) {
            inventory[item] = currentQuantity - quantity
        } else {
            println("Insufficient quantity of $item in stock.")
        }
    }

    fun displayInventory() {
        println("Current Inventory:")
        for ((item, quantity) in inventory) {
            println("$item: $quantity")
        }
    }

    fun getQuantity(item: String): Int {
        return inventory.getOrDefault(item, 0)
    }
}

// Класс информационной системы магазина электроники
class ElectronicsStoreSystem {
    val store = ElectronicsStore()
    private val users: MutableMap<String, User> = mutableMapOf()

    fun addUser(user: User) {
        users[user.username] = user
    }

    fun authenticate(username: String, password: String): User? {
        val user = users[username]
        return if (user?.password == password) {
            user
        } else {
            null
        }
    }

    fun performAction(user: User, action: String, vararg params: String) {
        when (user) {
            is Administrator -> {
                when (action) {
                    "add_item" -> {
                        val itemName = params[0]
                        val quantity = params[1].toInt()
                        store.addItem(itemName, quantity)
                    }
                    "remove_item" -> {
                        val itemName = params[0]
                        val quantity = params[1].toInt()
                        store.removeItem(itemName, quantity)
                    }
                    "display_inventory" -> {
                        store.displayInventory()
                    }
                    else -> println("Unknown action")
                }
            }
            is Guest -> {
                when (action) {
                    "display_inventory" -> {
                        store.displayInventory()
                    }
                    else -> println("Unknown action")
                }
            }
            is Storekeeper -> {
                when (action) {
                    "add_item" -> {
                        val itemName = params[0]
                        val quantity = params[1].toInt()
                        store.addItem(itemName, quantity)
                    }
                    "display_inventory" -> {
                        store.displayInventory()
                    }
                    else -> println("Unknown action")
                }
            }
        }
    }
}

fun main() {
    val scanner = Scanner(System.`in`)
    val system = ElectronicsStoreSystem()

    // Создаем пользователей
    println("Создание пользователей:")
    println("Введите данные для администратора:")
    val adminUsername = scanner.nextLine()
    val adminPassword = scanner.nextLine()
    val admin = Administrator(adminUsername, adminPassword)

    println("Введите данные для гостя:")
    val guestUsername = scanner.nextLine()
    val guestPassword = scanner.nextLine()
    val guest = Guest(guestUsername, guestPassword)

    println("Введите данные для складовщика:")
    val storekeeperUsername = scanner.nextLine()
    val storekeeperPassword = scanner.nextLine()
    val storekeeper = Storekeeper(storekeeperUsername, storekeeperPassword)

    system.addUser(admin)
    system.addUser(guest)
    system.addUser(storekeeper)

    // Вход в систему
    println("Вход в систему:")
    println("Введите имя пользователя:")
    val username = scanner.nextLine()
    println("Введите пароль:")
    val password = scanner.nextLine()

    val authenticatedUser = system.authenticate(username, password)
    authenticatedUser?.let { user ->
        when (user) {
            is Administrator -> {
                println("Вы вошли как администратор.")
                user.displayUsers()
            }
            is Guest -> {
                println("Вы вошли как гость.")
                user.viewItems(system.store)
            }
            is Storekeeper -> {
                println("Вы вошли как складовщик.")
                println("Доступные действия:")
                println("1. Проверить количество товара на складе")
                println("2. Добавить товар на склад")
                val choice = scanner.nextInt()
                scanner.nextLine() // очистка буфера
                when (choice) {
                    1 -> {
                        println("Введите название товара:")
                        val item = scanner.nextLine()
                        println("Количество товара на складе: ${user.checkStock(system.store, item)}")
                    }
                    2 -> {
                        println("Введите название товара:")
                        val item = scanner.nextLine()
                        println("Введите количество товара:")
                        val quantity = scanner.nextInt()
                        scanner.nextLine() // очистка буфера
                        user.addStock(system.store, item, quantity)
                        println("Товар успешно добавлен на склад.")
                    }
                    else -> println("Неправильный выбор.")
                }
            }
        }
    } ?: println("Неправильное имя пользователя или пароль.")
}
