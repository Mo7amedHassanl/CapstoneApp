import com.m7md7sn.capstoneApp.ui.screen.control.ControlViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ControlViewModelTest {
    private lateinit var viewModel: ControlViewModel

    @Before
    fun setup() {
        viewModel = ControlViewModel()
    }

    @Test
    fun initialPumpStatesAreAllFalse() {
        runTest {
            assertTrue(viewModel.pumpStates.value.all { it == false })
        }
    }

    @Test
    fun togglePumpChangesTheStateCorrectly() {
        runTest {
            viewModel.togglePump(0)
            assertTrue(viewModel.pumpStates.value[0])
            viewModel.togglePump(0)
            assertFalse(viewModel.pumpStates.value[0])
        }
    }

    @Test
    fun togglePumpTogglesOnlyTheSelectedPump() {
        runTest {
            val initial = viewModel.pumpStates.value.toList()
            viewModel.togglePump(2)
            val after = viewModel.pumpStates.value
            for (i in initial.indices) {
                if (i == 2) {
                    assertNotEquals(initial[i], after[i])
                } else {
                    assertEquals(initial[i], after[i])
                }
            }
        }
    }
} 