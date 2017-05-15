import javax.inject.{Inject, Singleton}

import filters.MetricsFilter
import play.api.http.DefaultHttpFilters

/**
  * This class configures filters that run on every request. This
  * class is queried by Play to get a list of filters.
  *
  * This is the filter that is used by the devo configuration.
  *
  * @param metricsFilter A demonstration filter that adds timing information to
  * each response.
  */
@Singleton
class DevoFilters @Inject() (metricsFilter: MetricsFilter)
  extends DefaultHttpFilters(metricsFilter)