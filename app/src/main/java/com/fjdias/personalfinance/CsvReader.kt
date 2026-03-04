package com.fjdias.personalfinance

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.PrintWriter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CsvReader {

    fun readTransactions(inputStream: InputStream): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        try {
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                // Skip header
                reader.readLine()
                
                var line: String? = reader.readLine()
                while (line != null) {
                    val parts = parseCsvLine(line)
                    if (parts.size >= 3) {
                        try {
                            val date = LocalDate.parse(parts[0], formatter)
                            val title = parts[1]
                            val amount = parts[2].toDouble()
                            
                            // Se houver uma 4ª coluna (categoria), usamos ela. 
                            // Caso contrário, usamos a lógica de categorização.
                            var categoryName = if (parts.size >= 4 && parts[3].isNotBlank()) {
                                parts[3]
                            } else {
                                "Outros"
                            }

                            // Forçar "Renda" se a categoria for "renda" (case insensitive)
                            if (categoryName.equals("renda", ignoreCase = true)) {
                                categoryName = "Renda"
                            }

                            transactions.add(Transaction(date = date, title = title, amount = amount, categoryName = categoryName))
                        } catch (e: Exception) {
                            // Skip malformed lines
                        }
                    }
                    line = reader.readLine()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return transactions
    }

    fun exportTransactions(outputStream: OutputStream, transactions: List<Transaction>) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        PrintWriter(outputStream).use { writer ->
            writer.println("date,title,amount,category")
            transactions.forEach { t ->
                val titleEscaped = if (t.title.contains(",")) "\"${t.title}\"" else t.title
                writer.println("${t.date.format(formatter)},$titleEscaped,${t.amount},${t.categoryName}")
            }
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false
        
        for (char in line) {
            when {
                char == '\"' -> inQuotes = !inQuotes
                char == ',' && !inQuotes -> {
                    result.add(current.toString().trim())
                    current = StringBuilder()
                }
                else -> current.append(char)
            }
        }
        result.add(current.toString().trim())
        return result
    }
}
