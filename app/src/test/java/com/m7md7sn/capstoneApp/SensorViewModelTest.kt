import com.m7md7sn.capstoneApp.data.repository.SensorRepository
import com.m7md7sn.capstoneApp.ui.screen.sensor.SensorViewModel
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
class SensorViewModelTest {
    private lateinit var repo: SensorRepository
    private lateinit var viewModel: SensorViewModel

    @Before
    fun setup() {
        repo = mockk(relaxed = true)
        viewModel = SensorViewModel(repo)
    }

    @Test
    fun initialStateIsLoading() {
        runTest {
            assertTrue(viewModel.isLoading.value)
        }
    }

    @Test
    fun loadsSensorReadings() {
        runTest {
            val repo = mockk<SensorRepository>()
            val readings = listOf(mockk<com.m7md7sn.capstoneApp.data.model.TimedSensorReading>())
            coEvery { repo.getTimedSensorReadings(any()) } returns readings
            coEvery { repo.getTimedSensorReadingsFlow(any()) } returns flowOf(readings)
            val viewModel = SensorViewModel(repo)
            viewModel.loadReadings("ph")
            kotlinx.coroutines.delay(10)
            assertEquals(readings, viewModel.readings.value)
        }
    }

    @Test
    fun handlesErrorsFromRepository() {
        runTest {
            val repo = mockk<SensorRepository>()
            coEvery { repo.getTimedSensorReadings(any()) } returns emptyList()
            coEvery { repo.getTimedSensorReadingsFlow(any()) } returns flowOf(throw RuntimeException("Test error"))
            val viewModel = SensorViewModel(repo)
            viewModel.loadReadings("ph")
            kotlinx.coroutines.delay(10)
            assertNotNull(viewModel.error.value)
        }
    }

    @Test
    fun refreshReloadsReadings() {
        runTest {
            val readings = listOf(mockk<com.m7md7sn.capstoneApp.data.model.TimedSensorReading>())
            coEvery { repo.getTimedSensorReadings(any()) } returns readings
            coEvery { repo.getTimedSensorReadingsFlow(any()) } returns flowOf(readings)
            viewModel.refresh("ph")
            kotlinx.coroutines.delay(10)
            assertEquals(readings, viewModel.readings.value)
        }
    }
} 