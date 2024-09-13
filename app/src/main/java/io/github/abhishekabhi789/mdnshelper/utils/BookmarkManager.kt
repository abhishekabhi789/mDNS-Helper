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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarkManager @Inject constructor(@ApplicationContext private val context: Context) {
    private var sharedPreferences: SharedPreferences =
        context.getSharedPreferences(BOOKMARKS_PREF_NAME, Context.MODE_PRIVATE)
    private var _bookmarks = MutableStateFlow<Set<Pair<String, String>>>(emptySet())
    val bookmarks = _bookmarks.asStateFlow()

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
        _bookmarks.update { bookmarkSet }
    }

    fun isBookmarked(info: MdnsInfo): Boolean {
        return _bookmarks.value.any { it.first == info.getServiceType() && it.second == info.getServiceName() }
    }

    fun addBookMark(info: MdnsInfo) {
        _bookmarks.update { bookmarksSet ->
            val mutableSet = bookmarksSet.toMutableSet()
            val bookmarkItem = Pair(info.getServiceType(), info.getServiceName())
            mutableSet.add(bookmarkItem)
            val savableSet = mutableSet.map { it.first + BOOKMARK_SEPARATOR + it.second }.toSet()
            sharedPreferences.edit().putStringSet(BOOKMARK_LIST_KEY, savableSet).apply()
            mutableSet
        }
        refreshBookmarks()


    }

    fun removeBookmark(info: MdnsInfo) {
        _bookmarks.update { bookmarksSet ->
            val mutableSet = bookmarksSet.toMutableSet()
            val bookmarkItem = Pair(info.getServiceType(), info.getServiceName())
            mutableSet.remove(bookmarkItem)
            val savableSet = mutableSet.map { it.first + BOOKMARK_SEPARATOR + it.second }.toSet()
            sharedPreferences.edit().putStringSet(BOOKMARK_LIST_KEY, savableSet).apply()
            mutableSet
        }

        refreshBookmarks()
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
