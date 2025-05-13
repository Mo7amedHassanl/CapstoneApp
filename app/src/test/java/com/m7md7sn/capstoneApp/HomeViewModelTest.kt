import com.m7md7sn.capstoneApp.data.repository.SensorRepository
import com.m7md7sn.capstoneApp.ui.screen.home.HomeViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private lateinit var repo: SensorRepository
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        repo = mockk(relaxed = true)
        viewModel = HomeViewModel(repo)
    }

    @Test
    fun initialStateIsLoading() {
        runTest {
            assertTrue(viewModel.isLoading.value)
        }
    }

    @Test
    fun loadsSensorReadingsAndSystemParts() {
        runTest {
            val readings = listOf(mockk<com.m7md7sn.capstoneApp.data.model.SensorReading>())
            val parts = listOf(mockk<com.m7md7sn.capstoneApp.data.model.SystemPart>())
            coEvery { repo.getSensorReadings() } returns readings
            coEvery { repo.getSystemParts() } returns parts
            coEvery { repo.getSensorReadingsFlow() } returns flowOf(readings)
            // Wait for coroutine
            kotlinx.coroutines.delay(10)
            assertEquals(readings, viewModel.sensorReadings.value)
            assertEquals(parts, viewModel.systemParts.value)
        }
    }

    @Test
    fun handlesErrorsFromRepository() {
        runTest {
            val readings = emptyList<com.m7md7sn.capstoneApp.data.model.SensorReading>()
            val parts = emptyList<com.m7md7sn.capstoneApp.data.model.SystemPart>()
            coEvery { repo.getSensorReadings() } returns readings
            coEvery { repo.getSystemParts() } returns parts
            coEvery { repo.getSensorReadingsFlow() } returns flowOf(throw RuntimeException("Test error"))
            kotlinx.coroutines.delay(10)
            assertNotNull(viewModel.error.value)
        }
    }

    @Test
    fun refreshReloadsData() {
        runTest {
            val readings = listOf(mockk<com.m7md7sn.capstoneApp.data.model.SensorReading>())
            val parts = listOf(mockk<com.m7md7sn.capstoneApp.data.model.SystemPart>())
            coEvery { repo.getSensorReadings() } returns readings
            coEvery { repo.getSystemParts() } returns parts
            coEvery { repo.getSensorReadingsFlow() } returns flowOf(readings)
            viewModel.refresh()
            kotlinx.coroutines.delay(10)
            assertEquals(readings, viewModel.sensorReadings.value)
            assertEquals(parts, viewModel.systemParts.value)
        }
    }
} 