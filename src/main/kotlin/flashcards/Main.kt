package flashcards

import java.io.File

fun definitionExists(definition: String): Boolean = cards.containsValue(definition)
fun termExists(term: String): Boolean = cards.containsKey(term)
fun getKeysFromValue(key: String): String = cards.filterValues { it == key }.keys.first()

var fileWrite: File? = null

// the list contains all the input and output of the program
val logList = mutableListOf<String>()

// the map contains the card
val cards = mutableMapOf<String, String>()

// this map contains for each card the number of errors made
val cardError = mutableMapOf<String, Int>()

// it contains the name of input file
var inputFile = ""

// it contains the name of output file
var outputFile = ""

fun main(args: Array<String>) {
    parseArguments(args)

    // if there is an import file, import it
    if (inputFile.isNotBlank()) {
        importCards(inputFile)
    }

    printMenu()

    // if there is an export file, generate it
    if (outputFile.isNotBlank()) {
        exportCards(outputFile)
    }
}

fun parseArguments(args: Array<String>) {
    for (index in args.indices step 2) {
        when (args[index]) {
            "-import" -> inputFile = args[index + 1]
            "-export" -> outputFile = args[index + 1]
        }
    }
}

// this functions print to screen and fill the list containing the log
fun printLog(value: String) {
    logList.add(value)
    println(value)
}

// this function read from console and fill the list containing the log
fun readLog(): String {
    val value: String = readln()
    logList.add(value)
    return value
}

// print the menu
fun printMenu() {
    while (true) {
        printLog("Input the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):")
        when (readLog()) {
            "add" -> createCard()
            "remove" -> removeCard()
            "import" -> importCards()
            "export" -> exportCards()
            "ask" -> play()
            "exit" -> {
                printLog("Bye bye!")
                break
            }

            "log" -> writeToLog()
            "hardest card" -> hardestCard()
            "reset stats" -> resetStats()
        }
    }
}

// reset statistics
fun resetStats() {
    // iterate for each cardError and set the error counter to 0
    for ((k, _) in cardError) {
        cardError[k] = 0
    }
    printLog("Card statistics have been reset.\n")
}

// find the hardest card
fun hardestCard() {
    // get the cards with errors, then display them
    var totalErrors = 0

    // a counter to determine if the card is one or more with errors
    var counter = 0

    // get the maximum number for errors and extract related cards.
    val maxEntry = cardError.values.maxOfOrNull { it }

    // extract all cards with specified max value
    val extractedCards = cardError.filterValues { it == maxEntry }
    val cardsWrong = extractedCards.keys.joinToString("\", \"", "\"", "\"")

    // iterate the extracted cards to get error values
    for ((k, _) in extractedCards) {
        val error = extractedCards.getValue(k)
        if (error > 0) {
            counter++
            totalErrors += error
        }
    }

    // display a summary
    when (counter) {
        0 -> printLog("There are no cards with errors.\n")
        1 -> printLog("The hardest card is $cardsWrong. You have $totalErrors errors answering it.\n")
        else -> printLog("The hardest cards are $cardsWrong. You have $totalErrors errors answering them.\n")
    }
}

// write to a log file
fun writeToLog() {
    // get the filename
    val filename = getAbsolutePath()
    // create the file to write to
    fileWrite = File(filename)
    for (line in logList) {
        fileWrite!!.appendText("$line\n")
    }

    printLog("The log has been saved.\n")
}

// get the absolute path to a file
fun getAbsolutePath(): String {
    printLog("File name:")
    // get the filename
    val filename = readLog()

    // get the working directory
    val workingDirectory = System.getProperty("user.dir")
    // determine separator from OS
    val separator = File.separator
    // return the absolute path to file
    return "$workingDirectory$separator$filename"
}

// import the cards from file
fun importCards(argument: String = "") {

    val filename: String = argument.ifBlank {
        // get the filename from console
        getAbsolutePath()
    }

    // get the file to read from
    val fileRead = File(filename)
    // check if file exists
    if (fileRead.exists()) {

        // read the content of file
        val lines = fileRead.readLines()

        // create map from content and iterate counter of lines
        for (index in lines.indices step 3) {
            cards[lines[index]] = lines[index + 1]
            cardError[lines[index]] = lines[index + 2].toInt()
        }

        // display the number of cards read
        printLog("${lines.size / 3} cards have been loaded.\n")
    } else {
        printLog("File not found.\n")
    }
}

// export the cards to file
fun exportCards(argument: String = "") {

    val filename: String = argument.ifBlank {
        // get the filename from console
        getAbsolutePath()
    }

    // create the file to write to
    val fileWrite = File(filename)
    // delete the file
    fileWrite.delete()
    // write key and value on separate lines for each card
    for ((k, v) in cards) {
        val errorCount = cardError[k]
        fileWrite.appendText("$k\n$v\n$errorCount\n")
    }

    // print the number of cards saved on file
    printLog("${cards.size} cards have been saved.\n")
}

// create a card
fun createCard() {
    // get term
    printLog("The card:")
    val term = readLog()
    if (termExists(term)) {
        printLog("The card \"$term\" already exists.\n")
        return
    }

    // get definition
    printLog("The definition of the card:")
    val definition = readLog()
    if (definitionExists(definition)) {
        printLog("The definition \"$definition\" already exists.\n")
        return
    }

    // add the card and error counter
    cards[term] = definition
    cardError[term] = 0

    printLog("The pair (\"$term\":\"$definition\") has been added\n")
}

// remove a card
fun removeCard() {
    // ask for which term to ve removed
    printLog("Which card?")
    val term = readLog()

    val result = cards.remove(term)
    if (result != null) {
        // if the card has been removed, remove the error counter too
        cardError.remove(term)
        printLog("The card has been removed.\n")
    } else {
        printLog("Can't remove \"$term\": there is no such card.\n")
    }
}

// play the game
fun play() {
    // asks how many times to play
    printLog("How many times to ask?")
    val times = readLog().toInt()

    // when playedCards == times, stop the game
    var playedCards = 0

    // get the total number of cards
    val totalCards = cards.size

    // iterations
    var iteration = 0

    // iterate cards
    for ((k, v) in cards) {
        if (iteration == times || times > totalCards) {
            printLog("")
            return
        }

        printLog("Print the definition of \"$k\":")
        val termDefinition = readLog()

        // you guessed the definition
        if (termDefinition == v) {
            printLog("Correct!\n")
        } else if (definitionExists(termDefinition)) {
            // definition is wrong for this card
            printLog(
                "Wrong. The right answer is \"$v\", but your definition is correct for \"${
                    getKeysFromValue(
                        termDefinition
                    )
                }\"."
            )
            // update counter error for specific term
            val previousError = (cardError[k]?.plus(1))
            if (previousError != null) {
                cardError[k] = previousError
            }
        } else {
            // definition is wrong at all
            printLog("Wrong. The right answer is \"$v\".")

            // update counter error for specific term
            val previousError = (cardError[k]?.plus(1))
            if (previousError != null) {
                cardError[k] = previousError
            }
            // increment the card played so far
            playedCards++
        }
        iteration++
    }
}