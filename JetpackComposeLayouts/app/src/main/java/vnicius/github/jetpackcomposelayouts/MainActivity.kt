package vnicius.github.jetpackcomposelayouts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import coil.compose.rememberImagePainter
import kotlinx.coroutines.launch
import vnicius.github.jetpackcomposelayouts.ui.theme.JetpackComposeLayoutsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JetpackComposeLayoutsTheme {
                LayoutsCodelab()
//                ImageList()
            }
        }
    }
}

@Composable
fun PhotographCard(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .padding(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colors.surface)
            .clickable { }
            .padding(16.dp)
    ) {
        Surface(
            modifier = Modifier.size(50.dp),
            shape = CircleShape,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.2f)
        ) {
            // Image placeholder
        }
        Column(
            modifier = Modifier
                .padding(start = 8.dp)
                .align(Alignment.CenterVertically)
        ) {
            Text(text = "Alfred Sisley", fontWeight = FontWeight.Bold)
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Text(text = "3 minutes ago", style = MaterialTheme.typography.body2)
            }
        }
    }
}

@Preview
@Composable
fun PhotographCardPreview() {
    JetpackComposeLayoutsTheme {
        PhotographCard()
    }
}

@Composable
fun LayoutsCodelab() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("LayoutsCodelab")
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Filled.Favorite, contentDescription = null)
                    }
                }
            )
        }
    ) { innerPadding ->
        StaggeredLayoutBodyContent(
            Modifier
                .padding(innerPadding)
                .padding(8.dp)
        )
    }
}

@Composable
private fun BodyContent(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = "Hi there!")
        Text(text = "Thanks for going through the Layouts codelab")
    }
}

@Preview
@Composable
fun LayoutsCodelabPreview() {
    JetpackComposeLayoutsTheme {
        LayoutsCodelab()
    }
}

@Composable
fun ImageList() {
    val listSize = 100
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Column {
        Row {
            Button(onClick = {
                coroutineScope.launch {
                    scrollState.animateScrollToItem(0)
                }
            }) {
                Text(text = "Scroll to start")
            }

            Button(onClick = {
                coroutineScope.launch {
                    scrollState.animateScrollToItem(listSize - 1)
                }
            }) {
                Text("Scroll to end")
            }
        }
        LazyColumn(state = scrollState) {
            items(listSize) {
                ImageListItem(index = it)
            }
        }
    }
}

@Composable
fun ImageListItem(index: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = rememberImagePainter(data = "https://developer.android.com/images/brand/Android_Robot.png"),
            contentDescription = "Android logo",
            modifier = Modifier.size(50.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text("Item $index", style = MaterialTheme.typography.subtitle1)
    }
}

// region Using layout modifier
fun Modifier.firstBaselineTop(
    firstBaselineToDp: Dp
) = this.then(
    layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)

        check(placeable[FirstBaseline] != AlignmentLine.Unspecified)
        val firstBaseline = placeable[FirstBaseline]

        val placeableY = firstBaselineToDp.roundToPx() - firstBaseline
        val height = placeable.height + placeableY

        layout(placeable.width, height) {
            placeable.placeRelative(0, placeableY)
        }
    }
)

@Preview
@Composable
fun TextWithPaddingToBaselinePreview() {
    JetpackComposeLayoutsTheme {
        Text("Hi there!", modifier = Modifier.firstBaselineTop(32.dp))
    }
}

@Preview
@Composable
fun TextWithNormalPaddingPreview() {
    JetpackComposeLayoutsTheme {
        Text("Hi there!", modifier = Modifier.padding(top = 32.dp))
    }
}

// endregion

// region Using the Layout Composable
@Composable
fun MyOwnColumn(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Layout(modifier = modifier, content = content) { measurables, constraints ->
        val placeables = measurables.map {
            it.measure(constraints)
        }
        var yPosition = 0

        // constraints.maxWidth and constraints.maxHeight will create the layout size
        // based on the parent max width and height

        layout(constraints.maxWidth, constraints.maxHeight) {
            placeables.forEach { placeable ->
                placeable.placeRelative(0, yPosition)
                yPosition += placeable.height
            }
        }
    }
}

@Preview
@Composable
fun MyOwnColumnPreview() {
    JetpackComposeLayoutsTheme {
        MyOwnColumn {
            Text(text = "Hi there!")
            Text(text = "Hi there!")
            Text(text = "Hi there!")
            Text(text = "Hi there!")
            Text(text = "Hi there!")
        }
    }
}

// endregion

// region Complex custom layout

@Composable
fun StaggeredGrid(
    modifier: Modifier = Modifier,
    rows: Int = 3,
    content: @Composable () -> Unit
) {
    Layout(content = content, modifier = modifier) { measurables, constraints ->
        val rowsWidths = IntArray(rows) { 0 }
        val rowsHeights = IntArray(rows) { 0 }

        val placeables = measurables.mapIndexed { index, measurable ->
            val placeable = measurable.measure(constraints)

            val row = index % rows
            rowsWidths[row] += placeable.width
            rowsHeights[row] = Math.max(rowsHeights[row], placeable.height)

            placeable
        }

        val width = rowsWidths.maxOrNull()?.coerceIn(
            constraints.minWidth.rangeTo(constraints.maxWidth)
        ) ?: constraints.minWidth
        val height = rowsHeights.sum()
            .coerceIn(constraints.minHeight.rangeTo(constraints.maxHeight))

        val rowY = IntArray(rows) { 0 }

        for (i in 1 until rows) {
            rowY[i] = rowY[i - 1] + rowsHeights[i - 1]
        }

        layout(width, height) {
            val rowX = IntArray(rows) { 0 }

            placeables.forEachIndexed { index, placeable ->
                val row = index % rows

                placeable.placeRelative(
                    x = rowX[row],
                    y = rowY[row]
                )

                rowX[row] += placeable.width
            }
        }
    }
}

@Composable
fun Chip(text: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        border = BorderStroke(color = Color.Black, width = Dp.Hairline),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(MaterialTheme.colors.secondary)
            )
            Spacer(Modifier.width(4.dp))
            Text(text = text)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChipPreview() {
    JetpackComposeLayoutsTheme {
        Chip(text = "Test")
    }
}

val topics = listOf(
    "Arts & Crafts", "Beauty", "Books", "Business", "Comics", "Culinary",
    "Design", "Fashion", "Film", "History", "Maths", "Music", "People", "Philosophy",
    "Religion", "Social sciences", "Technology", "TV", "Writing"
)

@Composable
fun StaggeredLayoutBodyContent(modifier: Modifier = Modifier) {
    Row(modifier = modifier.horizontalScroll(rememberScrollState())) {
        StaggeredGrid {
            topics.forEach { topic ->
                Chip(text = topic, modifier = Modifier.padding(8.dp))
            }
        }
    }
}

// endregion

// region ConstraintLayout

@Composable
fun ConstraintLayoutContent() {
    ConstraintLayout {
        // First create the references
        val (button1, button2, text) = createRefs()

        Button(
            onClick = { },
            modifier = Modifier.constrainAs(button1) {
                top.linkTo(parent.top, margin = 16.dp)
            }
        ) {
            Text("Button 1")
        }

        Text(
            "Text",
            modifier = Modifier.constrainAs(text) {
                top.linkTo(button1.bottom, margin = 16.dp)
                centerAround(button1.end)
            }
        )

        val barrier = createEndBarrier(button1, text)
        Button(
            onClick = { },
            modifier = Modifier.constrainAs(button2) {
                top.linkTo(parent.top, margin = 16.dp)
                start.linkTo(barrier)
            }
        ) {
            Text("Button 2")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ConstraintLayoutContentPreview() {
    JetpackComposeLayoutsTheme {
        ConstraintLayoutContent()
    }
}

@Composable
fun LargeConstraintLayout() {
    ConstraintLayout {
        val text = createRef()
        val guideline = createGuidelineFromStart(fraction = .5f)

        Text(
            "This is a very very very very very very very long text",
            Modifier.constrainAs(text) {
                linkTo(start = guideline, end = parent.end)
                width = Dimension.preferredWrapContent
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LargeConstraintLayoutPreview() {
    JetpackComposeLayoutsTheme {
        LargeConstraintLayout()
    }
}

@Composable
fun DecoupledConstraintLayout() {
    BoxWithConstraints {
        val constraints = if (maxWidth < maxHeight) {
            decoupledConstraints(16.dp)
        } else {
            decoupledConstraints(32.dp)
        }

        ConstraintLayout(constraints) {
            Button(onClick = { }, modifier = Modifier.layoutId("button")) {
                Text(text = "Button")
            }
            Text(text = "Text", modifier = Modifier.layoutId("text"))
        }
    }
}

private fun decoupledConstraints(margin: Dp): ConstraintSet =
    ConstraintSet {
        val button = createRefFor("button")
        val text = createRefFor("text")

        constrain(button) {
            top.linkTo(parent.top, margin = margin)
        }
        constrain(text) {
            top.linkTo(button.bottom, margin = margin)
        }
    }

@Preview(showBackground = true)
@Composable
fun DecoupledConstraintLayoutPreview() {
    JetpackComposeLayoutsTheme {
        DecoupledConstraintLayout()
    }
}

// endregion

// region Intrinsics

@Composable
fun TwoTexts(text1: String, text2: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier.height(IntrinsicSize.Min)) {
        Text(
            text = text1,
            modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp)
                .wrapContentWidth(Alignment.Start)
        )

        Divider(
            color = Color.Black,
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp)
        )

        Text(
            text = text2,
            modifier = Modifier
                .weight(1f)
                .padding(end = 4.dp)
                .wrapContentWidth(Alignment.End)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TwoTextsPeview() {
    JetpackComposeLayoutsTheme() {
        Surface {
            TwoTexts("Hi", "there")
        }
    }
}

// endregion
