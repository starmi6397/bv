package dev.aaa1115910.bv.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.tv.foundation.lazy.grid.TvGridCells
import androidx.tv.foundation.lazy.grid.TvGridItemSpan
import androidx.tv.foundation.lazy.grid.TvLazyGridState
import androidx.tv.foundation.lazy.grid.TvLazyVerticalGrid
import androidx.tv.foundation.lazy.grid.itemsIndexed
import dev.aaa1115910.bv.VideoInfoActivity
import dev.aaa1115910.bv.component.videocard.SmallVideoCard
import dev.aaa1115910.bv.entity.VideoCardData
import dev.aaa1115910.bv.viewmodel.home.DynamicViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun DynamicsScreen(
    modifier: Modifier = Modifier,
    tvLazyGridState: TvLazyGridState,
    dynamicViewModel: DynamicViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    if (dynamicViewModel.isLogin) {

        TvLazyVerticalGrid(
            modifier = modifier,
            state = tvLazyGridState,
            columns = TvGridCells.Fixed(4),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            itemsIndexed(dynamicViewModel.dynamicList) { index, dynamic ->
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    SmallVideoCard(
                        data = VideoCardData(
                            avid = dynamic.modules.moduleDynamic.major?.archive?.aid?.toInt()
                                ?: 170001,
                            title = dynamic.modules.moduleDynamic.major?.archive?.title ?: "",
                            cover = dynamic.modules.moduleDynamic.major?.archive?.cover ?: "",
                            playString = dynamic.modules.moduleDynamic.major?.archive?.stat?.play
                                ?: "",
                            danmakuString = dynamic.modules.moduleDynamic.major?.archive?.stat?.danmaku
                                ?: "",
                            upName = dynamic.modules.moduleAuthor.name,
                            timeString = dynamic.modules.moduleDynamic.major?.archive?.durationText
                                ?: ""
                        ),
                        onClick = {
                            VideoInfoActivity.actionStart(
                                context,
                                dynamic.modules.moduleDynamic.major!!.archive!!.aid.toInt()
                            )
                        },
                        onFocus = {
                            if (index + 24 > dynamicViewModel.dynamicList.size) {
                                scope.launch(Dispatchers.Default) { dynamicViewModel.loadMore() }
                            }
                        }
                    )
                }
            }
            if (dynamicViewModel.loading)
                item(
                    span = { TvGridItemSpan(4) }
                ) {
                    Text(
                        text = "Loading",
                        color = Color.White
                    )
                }

            if (!dynamicViewModel.hasMore)
                item(
                    span = { TvGridItemSpan(4) }
                ) {
                    Text(
                        text = "没有更多了捏",
                        color = Color.White
                    )
                }
        }
    } else {
        Box(
            modifier = Modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(text = "请先登录")
        }
    }
}
