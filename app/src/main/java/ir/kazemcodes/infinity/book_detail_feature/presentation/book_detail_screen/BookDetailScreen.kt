package ir.kazemcodes.infinity.book_detail_feature.presentation.book_detail_screen


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zhuinden.simplestackcomposeintegration.core.LocalBackstack
import ir.kazemcodes.infinity.api_feature.HttpSource
import ir.kazemcodes.infinity.base_feature.navigation.ChapterDetailKey
import ir.kazemcodes.infinity.base_feature.navigation.WebViewKey
import ir.kazemcodes.infinity.book_detail_feature.presentation.book_detail_screen.components.ButtonWithIconAndText
import ir.kazemcodes.infinity.book_detail_feature.presentation.book_detail_screen.components.CardTileComposable
import ir.kazemcodes.infinity.book_detail_feature.presentation.book_detail_screen.components.DotsFlashing
import ir.kazemcodes.infinity.book_detail_feature.presentation.book_detail_screen.components.ExpandingText
import ir.kazemcodes.infinity.explore_feature.data.model.Book
import ir.kazemcodes.infinity.presentation.screen.components.BookImageComposable


@Composable
fun BookDetailScreen(
    modifier: Modifier = Modifier,
    book: Book = Book.create(),
    viewModel: BookDetailViewModel = hiltViewModel(),
    api: HttpSource
) {
    val detailState = viewModel.detailState.value
    val chapterState = viewModel.chapterState.value

    LaunchedEffect(key1 = true) {
        viewModel.getBookData(book)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (!viewModel.detailState.value.book.description.isNullOrBlank()) {

            BookDetailScreenLoadedComposable(
                modifier = modifier,
                viewModel = viewModel,
                api = api
            )

        }
        if (detailState.error.isNotBlank()) {
            Text(
                text = detailState.error,
                color = MaterialTheme.colors.error,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .align(Alignment.Center)
            )
        }
        if (detailState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }

    }


}


@Composable
fun BookDetailScreenLoadedComposable(
    modifier: Modifier = Modifier,
    viewModel: BookDetailViewModel,
    api: HttpSource
) {
    val backStack = LocalBackstack.current
    val inLibrary = viewModel.inLibrary.value
    val book = viewModel.detailState.value.book
    val chapters = viewModel.chapterState.value.chapters
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            /** Top Most Bar**/
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    modifier = modifier.clickable { backStack.goBack() },
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "back Button",
                    tint = MaterialTheme.colors.onBackground
                )
                Row(horizontalArrangement = Arrangement.Center) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "WebView",
                        tint = MaterialTheme.colors.onBackground,
                        modifier = Modifier
                            .clickable {
                                backStack.goTo(WebViewKey(book.link))
                            }
                    )
                    Spacer(modifier = modifier.width(16.dp))
                    Icon(
                        imageVector = Icons.Default.IosShare,
                        contentDescription = "more information",
                        tint = MaterialTheme.colors.onBackground
                    )
                }

            }
            Spacer(modifier = modifier.height(25.dp))
            /** Image and Book Information **/
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                /** Book Image **/
                BookImageComposable(
                    image = book.coverLink ?: "",
                    modifier = modifier
                        .width(120.dp)
                        .height(180.dp)
                        .shadow(8.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = modifier.width(8.dp))
                /** Book Info **/
                Column {
                    Text(
                        text = book.bookName,
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.onBackground,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Author: ${book.author}",
                        style = MaterialTheme.typography.subtitle2,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.onBackground.copy(alpha = .5f),
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Source: ${book.source}",
                        color = MaterialTheme.colors.onBackground.copy(alpha = .5f),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.subtitle2,
                    )
                    Text(
                        text = "Genre: ${book.category}",
                        color = MaterialTheme.colors.onBackground.copy(alpha = .5f),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.subtitle2,
                    )

                }


            }
            Divider(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )
            /** Book Summary **/
            Text(
                text = "Synopsis", fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.onBackground,
                style = MaterialTheme.typography.h6
            )
            ExpandingText(text = book.description ?: "Unknown")
            Divider(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )
            /** Chapter Content **/
            CardTileComposable(
                modifier = modifier.clickable {
                    backStack.goTo(ChapterDetailKey(chapters = chapters, book = book, api = api))
                },
                title = "Contents",
                subtitle = "${chapters.size} Chapters",
                trailing = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "", color = MaterialTheme.colors.onBackground,
                            style = MaterialTheme.typography.subtitle2
                        )
                        if (viewModel.chapterState.value.isLoading) {
                            DotsFlashing()
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Contents Detail",
                            tint = MaterialTheme.colors.onBackground,
                        )
                    }
                })
            Spacer(modifier = modifier.height(60.dp))
        }
        /** Botton Bar **/
        Box(
            modifier = modifier
                .background(MaterialTheme.colors.background)
                .height(60.dp)
                .align(Alignment.BottomCenter)
        ) {
            Divider()
            Row(
                modifier = modifier
                    .fillMaxWidth(),

                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically

            ) {
                ButtonWithIconAndText(
                    text = if (!inLibrary) "Add to Library" else "Added To Library",
                    imageVector = if (!inLibrary) Icons.Default.AddCircleOutline else Icons.Default.Check,
                    modifier = modifier
                        .clickable(role = Role.Button) {
                            if (!inLibrary) {
                                viewModel.insertBookDetailToLocal(
                                    book.copy(inLibrary = true, source = api.name).toBookEntity()
                                )
                                val chapterEntities = chapters.map {
                                    it.copy(bookName = book.bookName).toChapterEntity()
                                }
                                viewModel.insertChaptersToLocal(chapterEntities)
                                viewModel.toggleInLibrary()
                            } else {
                                viewModel.deleteLocalBook(book.bookName)
                                viewModel.deleteLocalChapters(book.bookName)
                                viewModel.insertBookDetailToLocal(
                                    book.copy(inLibrary = false, source = api.name).toBookEntity()
                                )
                                viewModel.toggleInLibrary()
                            }

                        }
                )
                Divider(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                )
                ButtonWithIconAndText(
                    text = "Continue Reading",
                    imageVector = Icons.Default.AutoStories
                )
                Divider(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)

                )
                ButtonWithIconAndText(
                    text = "Download",
                    imageVector = Icons.Default.FileDownload
                )
            }
        }
    }
}