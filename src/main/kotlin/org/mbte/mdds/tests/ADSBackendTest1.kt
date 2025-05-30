package org.mbte.mdds.tests

import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.mbte.mdds.util.DatabaseHandler
import org.mbte.mdds.util.getDirectChildren
import org.mbte.mdds.util.loadXmlFromFile
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun main(args: Array<String>) = runBlocking {
	val test = ADSBackendTest1()
	val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
	val userHome = File(System.getProperty("user.home"))
	val time = dateFormatter.format(LocalDateTime.now()).replace(" ", "_").replace(":", ";")
	val dbFile = File(userHome, "${test.javaClass.simpleName}-$time.db")
	dbFile.createNewFile()
	val dbHandler = DatabaseHandler("jdbc:sqlite:${dbFile.absolutePath}")

	// No longer needed, leaving for assessment purposes
	//	dbHandler.initContactsTable()

	// 1. Load the xml file 'ab.xml'
	val document = test.loadXml("src/main/resources/ab.xml");

	// 2. Load the address book contents from 'ab.xml'
	val addressBook: AddressBook = document?.let {
		test.loadAddressBook(document)
	} ?: run {
		System.err.println("Failed to load XML, Document is NULL")
		return@runBlocking
	}

	// 3. Insert each contact into the Database
	addressBook.contacts.forEach { contact ->
		dbHandler.insertContact(contact)
	}

	// 4. Insert each contact into the Database
	val contacts = dbHandler.getAllContacts()

	// 5. Convert the contacts from the Database into Json and write to file
	val outputJson = test.convertToJson(AddressBook(contacts))
	val output = File(userHome, "${test.javaClass.simpleName}-$time.json")
	test.printOutput(outputJson, output)

	println("Assessment complete.")
	println("Database file located at ${dbFile.absolutePath}")
	println("JSON output located at ${output.absolutePath}")
}

data class AddressBook(val contacts: List<Contact>)
data class Contact(
	val id: String, 
	val companyName: String, 
	val name: String, 
	val title: String, 
	val address: String, 
	val city: String, 
	val email: String,
	val region: String?,
	val zip: String?, 
	val country: String, 
	val phone: String, 
	val fax: String?
)

interface AddressBookInterface {
	fun loadXml(fileName: String): Document?
	fun loadAddressBook(doc: Document): AddressBook
	fun convertToJson(addressBook: AddressBook): JSONObject
	fun printOutput(json: JSONObject, output: File)
}

class ADSBackendTest1(): AddressBookInterface {

	// Convert fileName to File format and loadXML
	override fun loadXml(fileName: String): Document? {
		return loadXmlFromFile(File(fileName))
	}

	// Creating Contact objects based on document elements.
	override fun loadAddressBook(doc: Document): AddressBook {
		val contactElements: MutableSet<Element> = getDirectChildren(doc.documentElement)
		val contacts: MutableList<Contact> = mutableListOf()
		for (contactElement: Element in contactElements) {
			contacts.add(Contact(
				id = contactElement.getElementsByTagName("CustomerID").item(0).textContent,
				companyName = contactElement.getElementsByTagName("CompanyName").item(0).textContent,
				name = contactElement.getElementsByTagName("ContactName").item(0).textContent,
				title = contactElement.getElementsByTagName("ContactTitle").item(0).textContent,
				address = contactElement.getElementsByTagName("Address").item(0).textContent,
				city = contactElement.getElementsByTagName("City").item(0).textContent,
				email = contactElement.getElementsByTagName("Email").item(0).textContent,
				region = contactElement.getElementsByTagName("Region").item(0)?.textContent,
				zip = contactElement.getElementsByTagName("PostalCode").item(0)?.textContent,
				country = contactElement.getElementsByTagName("Country").item(0).textContent,
				phone = contactElement.getElementsByTagName("Phone").item(0).textContent,
				fax = contactElement.getElementsByTagName("Fax").item(0)?.textContent
			))
		}
		return AddressBook(contacts)
	}

	// Storing each AddressBook record in a JSONObject
	// Storing each JSONObject in JSONArray and returning as JSONObject
	override fun convertToJson(addressBook: AddressBook): JSONObject {
		val addressBookJsonFormat = JSONObject()
		val jsonArray = JSONArray()
		addressBook.contacts.forEach {
			val jsonObject = JSONObject()
			jsonObject.put("id", it.id)
			jsonObject.put("companyName", it.companyName)
			jsonObject.put("name", it.name)
			jsonObject.put("title", it.title)
			jsonObject.put("address", it.address)
			jsonObject.put("city", it.city)
			jsonObject.put("email", it.email)
			jsonObject.put("region", it.region)
			jsonObject.put("zip", it.zip)
			jsonObject.put("country", it.country)
			jsonObject.put("phone", it.phone)
			jsonObject.put("fax", it.fax)
			jsonArray.put(jsonObject)
		}
		addressBookJsonFormat.put("contacts", jsonArray)
		return addressBookJsonFormat
	}

	// Write text to file with indentFactor for legibility.
	override fun printOutput(json: JSONObject, output: File) {
		output.writeText(json.toString(4))
	}

}