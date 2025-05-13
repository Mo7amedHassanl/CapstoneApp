import com.m7md7sn.capstoneApp.data.repository.SensorRepository
import com.m7md7sn.capstoneApp.ui.screen.monitoring.MonitoringViewModel
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
class MonitoringViewModelTest {
    private lateinit var repo: SensorRepository
    private lateinit var viewModel: MonitoringViewModel

    @Before
    fun setup() {
        repo = mockk(relaxed = true)
        viewModel = MonitoringViewModel(repo)
    }

    @Test
    fun initialStateIsLoading() {
        runTest {
            assertTrue(viewModel.isLoading.value)
        }
    }

    @Test
    fun loadsSensorStatuses() {
        runTest {
            val repo = mockk<SensorRepository>()
            val statuses = listOf(mockk<com.m7md7sn.capstoneApp.data.model.SensorStatus>())
            coEvery { repo.getSensorStatuses() } returns statuses
            coEvery { repo.getSensorStatusesFlow() } returns flowOf(statuses)
            val viewModel = MonitoringViewModel(repo)
            kotlinx.coroutines.delay(10)
            assertEquals(statuses, viewModel.sensorStatuses.value)
        }
    }

    @Test
    fun handlesErrorsFromRepository() {
        runTest {
            val repo = mockk<SensorRepository>()
            coEvery { repo.getSensorStatuses() } returns emptyList()
            coEvery { repo.getSensorStatusesFlow() } returns flowOf(throw RuntimeException("Test error"))
            val viewModel = MonitoringViewModel(repo)
            kotlinx.coroutines.delay(10)
            assertNotNull(viewModel.error.value)
        }
    }

    @Test
    fun refreshReloadsStatuses() {
        runTest {
            val statuses = listOf(mockk<com.m7md7sn.capstoneApp.data.model.SensorStatus>())
            coEvery { repo.getSensorStatuses() } returns statuses
            coEvery { repo.getSensorStatusesFlow() } returns flowOf(statuses)
            viewModel.refresh()
            kotlinx.coroutines.delay(10)
            assertEquals(statuses, viewModel.sensorStatuses.value)
        }
    }
} 