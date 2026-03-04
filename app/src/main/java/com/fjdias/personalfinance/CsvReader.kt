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
                            // Se houver uma 4ª coluna (categoria), usamos ela. Caso contrário, categorizamos automaticamente.
                            val categoryName = if (parts.size >= 4 && parts[3].isNotBlank()) {
                                parts[3]
                            } else {
                                categorize(title, amount)
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
            // Header
            writer.println("date,title,amount,category")
            transactions.forEach { t ->
                val titleEscaped = if (t.title.contains(",")) "\"${t.title}\"" else t.title
                writer.println("${t.date.format(formatter)},$titleEscaped,${t.amount},${t.categoryName}")
            }
        }
    }

    private fun categorize(title: String, amount: Double): String {
        if (amount < 0) return "Entrada"
        
        val normalizedTitle = title.lowercase()
        return when {
            normalizedTitle.contains("ifood") || normalizedTitle.contains("restaurante") || 
            normalizedTitle.contains("delicias") || normalizedTitle.contains("sushi") ||
            normalizedTitle.contains("padaria") || normalizedTitle.contains("outback") ||
            normalizedTitle.contains("frutas") || normalizedTitle.contains("supermercado") ||
            normalizedTitle.contains("extra") || normalizedTitle.contains("carrefour") ||
            normalizedTitle.contains("hortifruti") || normalizedTitle.contains("swift") ||
            normalizedTitle.contains("vila das frutas") -> "Alimentação"
            
            normalizedTitle.contains("posto") || normalizedTitle.contains("nutag") || 
            normalizedTitle.contains("zul") || normalizedTitle.contains("estacion") ||
            normalizedTitle.contains("uber") -> "Transporte"
            
            normalizedTitle.contains("petlove") || normalizedTitle.contains("urbanpet") ||
            normalizedTitle.contains("vet") || normalizedTitle.contains("pet") -> "Saúde"
            
            normalizedTitle.contains("drogasil") || normalizedTitle.contains("drogaria") ||
            normalizedTitle.contains("raia") || normalizedTitle.contains("saude") -> "Saúde"
            
            normalizedTitle.contains("sony") || normalizedTitle.contains("playstation") ||
            normalizedTitle.contains("steam") || normalizedTitle.contains("nexusmods") ||
            normalizedTitle.contains("youtube") || normalizedTitle.contains("lazer") -> "Lazer"
            
            normalizedTitle.contains("nucel") || normalizedTitle.contains("seguro") ||
            normalizedTitle.contains("google") || normalizedTitle.contains("airbnb") ||
            normalizedTitle.contains("leroy") || normalizedTitle.contains("kabum") ||
            normalizedTitle.contains("mercadolivre") || normalizedTitle.contains("mercadopago") -> "Serviços"
            
            else -> "Outros"
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
