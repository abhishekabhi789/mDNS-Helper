package io.github.abhishekabhi789.mdnshelper.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.ui.graphics.vector.ImageVector
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.abhishekabhi789.mdnshelper.MdnsHelperViewModel
import io.github.abhishekabhi789.mdnshelper.MdnsInfo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarkManager @Inject constructor(@ApplicationContext private val context: Context) {
    private var sharedPreferences: SharedPreferences =
        context.getSharedPreferences(BOOKMARKS_PREF_NAME, Context.MODE_PRIVATE)
    private lateinit var bookmarks: Set<Pair<String, String>>

    init {
        refreshBookmarks()
    }

    private fun refreshBookmarks() {
        val savedBookMarks: Set<String>? =
            sharedPreferences.getStringSet(BOOKMARK_LIST_KEY, emptySet())
        Log.d(TAG, "getBookMarks: found ${savedBookMarks?.size ?: 0} bookmarks")
        val bookmarkSet = savedBookMarks?.map { item ->
            item.split(BOOKMARK_SEPARATOR).let { Pair(it.first(), it.last()) }
        }?.toSet() ?: emptySet()
        Log.d(TAG, "refreshBookmarks: updating bookmark list total items ${bookmarkSet.size}")
        bookmarks = bookmarkSet
    }

    fun isBookmarked(info: MdnsInfo): Boolean {
        return bookmarks.any { it.first == info.getServiceType() && it.second == info.getServiceName() }
    }

    fun addBookMark(info: MdnsInfo) {
        val tempSet = bookmarks.toMutableSet()
        val bookmarkItem = Pair(info.getServiceType(), info.getServiceName())
        tempSet.add(bookmarkItem)
        val savableSet = tempSet.map { it.first + BOOKMARK_SEPARATOR + it.second }.toSet()
        sharedPreferences.edit().putStringSet(BOOKMARK_LIST_KEY, savableSet).apply()
        bookmarks = tempSet
    }

    fun removeBookmark(info: MdnsInfo) {
        val tempSet = bookmarks.toMutableSet()
        val bookmarkItem = Pair(info.getServiceType(), info.getServiceName())
        tempSet.remove(bookmarkItem)
        val savableSet = tempSet.map { it.first + BOOKMARK_SEPARATOR + it.second }.toSet()
        sharedPreferences.edit().putStringSet(BOOKMARK_LIST_KEY, savableSet).apply()
    }

    enum class BookMarkAction(
        val label: String,
        val icon: ImageVector,
        val action: (viewModel: MdnsHelperViewModel, info: MdnsInfo) -> Unit
    ) {
        ADD(
            "Add to bookmark", Icons.Default.BookmarkBorder,
            { viewModel, info -> viewModel.addOrRemoveFromBookmark(info = info, add = true) }),
        REMOVE(
            "Remove from bookmarks", Icons.Default.Bookmark,
            { viewModel, info -> viewModel.addOrRemoveFromBookmark(info = info, add = false) }),

    }

    companion object {
        private const val TAG = "BookmarkManager"
        private const val BOOKMARKS_PREF_NAME = "bookmark_preferences"
        private const val BOOKMARK_LIST_KEY = "bookmarks"
        private const val BOOKMARK_SEPARATOR = ","
    }
}
