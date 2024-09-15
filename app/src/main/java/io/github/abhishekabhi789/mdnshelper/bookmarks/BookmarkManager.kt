package io.github.abhishekabhi789.mdnshelper.bookmarks

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.ui.graphics.vector.ImageVector
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.abhishekabhi789.mdnshelper.viewmodel.MainActivityViewmodel
import io.github.abhishekabhi789.mdnshelper.data.MdnsInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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

    fun addBookMark(info: MdnsInfo): Boolean {
        var success = false
        val bookmarkItem = Pair(info.getServiceType(), info.getServiceName())
        _bookmarks.update { bookmarksSet ->
            val updatedSet = bookmarksSet + bookmarkItem
            val savableSet = updatedSet.map { it.first + BOOKMARK_SEPARATOR + it.second }.toSet()
            success = sharedPreferences.edit().putStringSet(BOOKMARK_LIST_KEY, savableSet).commit()
            updatedSet
        }
        CoroutineScope(Dispatchers.IO).launch {
            refreshBookmarks()
        }
        return success
    }

    fun removeBookmark(info: MdnsInfo): Boolean {
        var status = false
        val bookmarkItem = Pair(info.getServiceType(), info.getServiceName())
        _bookmarks.update { bookmarksSet ->
            val mutableSet = bookmarksSet.toMutableSet()
            mutableSet.remove(bookmarkItem)
            val savableSet = mutableSet.map { it.first + BOOKMARK_SEPARATOR + it.second }.toSet()
            status = sharedPreferences.edit().putStringSet(BOOKMARK_LIST_KEY, savableSet).commit()
            mutableSet
        }
        CoroutineScope(Dispatchers.IO).launch {
            refreshBookmarks()
        }
        return status
    }

    enum class BookMarkAction(
        val label: String,
        val icon: ImageVector,
        val action: (viewModel: MainActivityViewmodel, info: MdnsInfo, onComplete: (success: Boolean) -> Unit) -> Unit
    ) {
        ADD(
            "Add to bookmarks", Icons.Default.BookmarkBorder,
            { viewModel, info, onComplete ->
                viewModel.addOrRemoveFromBookmark(info = info, add = true, onComplete = onComplete)
            }),
        REMOVE(
            "Remove from bookmarks", Icons.Default.Bookmark,
            { viewModel, info, onComplete ->
                viewModel.addOrRemoveFromBookmark(info = info, add = false, onComplete = onComplete)
            }),

    }

    companion object {
        private const val TAG = "BookmarkManager"
        private const val BOOKMARKS_PREF_NAME = "bookmark_preferences"
        private const val BOOKMARK_LIST_KEY = "bookmarks"
        private const val BOOKMARK_SEPARATOR = ","
    }
}
