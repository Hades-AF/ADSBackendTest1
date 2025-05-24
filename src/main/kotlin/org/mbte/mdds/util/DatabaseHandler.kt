package org.mbte.mdds.util

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import org.mbte.mdds.AddressBookQueries
import org.mbte.mdds.AddressDatabase
import org.mbte.mdds.tests.Contact
import java.sql.Connection
import java.sql.DriverManager

class DatabaseHandler(private val url: String) {

    // Creating in-memory SQLite Database
    private var driver: SqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY);
    private var queries: AddressBookQueries;

    // Initialize SQLDelight file with Database
    // Creating instance for accessing queries
	init {
        AddressDatabase.Schema.create(driver)

        val db = AddressDatabase(driver)
        queries = db.addressBookQueries
    }

    // No longer needed, leaving for assessment purposes
    fun initContactsTable() {}

    // Using insert query to add new record to the DB
    // SQLDelight handles with parameterized queries
    fun insertContact(contact: Contact) {
        runCatching {
            queries.insertContact(
                CustomerID = contact.id,
                CompanyName = contact.companyName,
                ContactName = contact.name,
                ContactTitle = contact.title,
                Address = contact.address,
                City = contact.city,
                Email = contact.email,
                Region = contact.region,
                PostalCode = contact.zip,
                Country = contact.country,
                Phone = contact.phone,
                Fax = contact.fax
            )
        }.onFailure {
            System.err.println("Failed to insert contact.")
            it.printStackTrace()
        }
    }
    
    fun getAllContacts(): List<Contact> {
		getConnection()?.use { connection ->
            val statement = connection.createStatement()
            val sql = "TODO"
			val result = kotlin.runCatching { statement.executeQuery(sql) }
			return if (result.isFailure) {
				System.err.println("Failed to execute SQL: $sql")
				result.exceptionOrNull()?.printStackTrace()
				emptyList()
			} else {
				//TODO
				emptyList()
			}
        }
        return emptyList()
	}

    private fun getConnection(): Connection? {
        return try {
            DriverManager.getConnection(url)
        } catch (e: Exception) {
            System.err.println("Failed to get connection to SQLite!!")
            e.printStackTrace()
            null
        }
    }
}