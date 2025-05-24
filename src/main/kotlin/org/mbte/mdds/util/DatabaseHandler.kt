package org.mbte.mdds.util

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.mbte.mdds.AddressBookQueries
import org.mbte.mdds.AddressDatabase
import org.mbte.mdds.tests.Contact
import java.sql.Connection

class DatabaseHandler(private val url: String) {

    // Creating in-memory SQLite Database
    private var driver: SqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY);
    private var database: AddressDatabase
    private var queries: AddressBookQueries;

    // Initialize SQLDelight file with Database
    // Creating instance for accessing queries
	init {
        AddressDatabase.Schema.create(driver)
        database = AddressDatabase(driver)
        queries = database.addressBookQueries
    }

    // No longer needed, leaving for assessment purposes
    fun initContactsTable() {}

    // Using insert query to add new record to the DB
    // SQLDelight handles with parameterized queries
    suspend fun insertContact(contact: Contact) {
        withContext(Dispatchers.IO) {
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
    }

    // Obtaining all records from AddressBook table and structuring them as a list
    // Mapping each record to correct Contact data class format
    suspend fun getAllContacts(): List<Contact> {
        return withContext(Dispatchers.IO) {
            runCatching {
                queries.selectAll()
                    .executeAsList()
                    .map {
                        Contact(
                            id = it.CustomerID,
                            companyName = it.CompanyName ?: "",
                            name = it.ContactName ?: "",
                            title = it.ContactTitle ?: "",
                            address = it.Address ?: "",
                            city = it.City ?: "",
                            email = it.Email ?: "",
                            region = it.Region,
                            zip = it.PostalCode,
                            country = it.Country ?: "",
                            phone = it.Phone ?: "",
                            fax = it.Fax
                        )
                    }
            }.getOrElse {
                System.err.println("Failed to retrieve contacts.")
                it.printStackTrace()
                emptyList()
            }
        }
	}

    // No longer needed, leaving for assessment purposes
    private fun getConnection(): Connection? {return null}
}