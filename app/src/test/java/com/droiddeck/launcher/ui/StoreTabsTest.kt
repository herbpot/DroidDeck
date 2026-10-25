package com.droiddeck.launcher.ui

import com.droiddeck.launcher.stores.Store
import org.junit.Assert.assertEquals
import org.junit.Test

class StoreTabsTest {
    @Test fun libraryAndStoreAmazonLibraryOnly() {
        assertEquals(listOf("library", "store"), tabsFor(Store.GOG))
        assertEquals(listOf("library", "store"), tabsFor(Store.ITCH))
        assertEquals(listOf("library"), tabsFor(Store.AMAZON))
        assertEquals(listOf("library"), tabsFor(Store.STOVE))
        assertEquals("library", tabFor(Store.STOVE, "store"))
        assertEquals("library", tabFor(Store.AMAZON, "store"))
        assertEquals("library", tabFor(Store.GOG, "all"))
        assertEquals("library", tabFor(Store.EPIC, "installed"))
        assertEquals("store", tabFor(Store.GOG, "store"))
    }
}
