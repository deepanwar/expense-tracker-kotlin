package com.example.expensetracker.util

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import com.example.expensetracker.model.ImportedContact

object ContactReader {
    fun readContact(context: Context, contactUri: Uri): ImportedContact? {
        val contentResolver = context.contentResolver

        val contactCursor = contentResolver.query(
            contactUri,
            arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.PHOTO_URI
            ),
            null,
            null,
            null
        ) ?: return null

        contactCursor.use { cursor ->
            if (!cursor.moveToFirst()) return null

            val contactId = cursor.getString(
                cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID)
            )
            val name = cursor.getString(
                cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME)
            )?.trim().orEmpty()

            if (name.isEmpty()) return null

            val photoUri = cursor.getString(
                cursor.getColumnIndexOrThrow(ContactsContract.Contacts.PHOTO_URI)
            )

            return ImportedContact(
                name = name,
                phone = readPhoneNumber(contentResolver, contactId),
                email = readEmail(contentResolver, contactId),
                photoUri = photoUri,
                contactId = contactId
            )
        }
    }

    private fun readPhoneNumber(
        contentResolver: android.content.ContentResolver,
        contactId: String
    ): String? {
        val phoneCursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
            arrayOf(contactId),
            "${ContactsContract.CommonDataKinds.Phone.IS_PRIMARY} DESC"
        ) ?: return null

        phoneCursor.use { cursor ->
            if (!cursor.moveToFirst()) return null
            return cursor.getString(
                cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
            )?.trim()
        }
    }

    private fun readEmail(
        contentResolver: android.content.ContentResolver,
        contactId: String
    ): String? {
        val emailCursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Email.ADDRESS),
            "${ContactsContract.CommonDataKinds.Email.CONTACT_ID} = ?",
            arrayOf(contactId),
            "${ContactsContract.CommonDataKinds.Email.IS_PRIMARY} DESC"
        ) ?: return null

        emailCursor.use { cursor ->
            if (!cursor.moveToFirst()) return null
            return cursor.getString(
                cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.ADDRESS)
            )?.trim()
        }
    }
}
