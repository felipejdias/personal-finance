package com.fjdias.personalfinance

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayInputStream
import java.time.LocalDate

class CsvReaderTest {

    private val csvReader = CsvReader()

    @Test
    fun `readTransactions should use category from 4th column if present`() {
        val csvData = """
            date,title,amount,category
            2023-10-01,Assinatura Streaming,29.90,Lazer
        """.trimIndent()
        
        val inputStream = ByteArrayInputStream(csvData.toByteArray())
        val transactions = csvReader.readTransactions(inputStream)
        
        assertEquals(1, transactions.size)
        assertEquals("Lazer", transactions[0].categoryName)
        assertEquals("Assinatura Streaming", transactions[0].title)
    }

    @Test
    fun `readTransactions should fallback to automatic categorization if category column is missing`() {
        // "ifood" deve ser categorizado como Alimentação pela lógica interna
        val csvData = """
            date,title,amount
            2023-10-01,ifood,50.00
        """.trimIndent()
        
        val inputStream = ByteArrayInputStream(csvData.toByteArray())
        val transactions = csvReader.readTransactions(inputStream)
        
        assertEquals(1, transactions.size)
        assertEquals("Alimentação", transactions[0].categoryName)
    }

    @Test
    fun `exportTransactions should include category column`() {
        val transactions = listOf(
            Transaction(
                date = LocalDate.of(2023, 10, 1),
                title = "Mercado",
                amount = 150.0,
                categoryName = "Alimentação"
            )
        )
        
        val outputStream = java.io.ByteArrayOutputStream()
        csvReader.exportTransactions(outputStream, transactions)
        
        val exportedContent = outputStream.toString()
        val expectedHeader = "date,title,amount,category"
        val expectedRow = "2023-10-01,Mercado,150.0,Alimentação"
        
        val lines = exportedContent.trim().split("\n")
        assertEquals(expectedHeader, lines[0].trim())
        assertEquals(expectedRow, lines[1].trim())
    }
}
