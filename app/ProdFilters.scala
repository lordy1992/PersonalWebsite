import javax.inject.{Inject, Singleton}

import filters.{MetricsFilter, TLSFilter}
import play.api.http.DefaultHttpFilters

/**
  * This class configures filters that run on every request. This
  * class is queried by Play to get a list of filters.
  *
  * This is the filter that is used by the prod configuration.
  *
  * @param tlsFilter A filter to redirect HTTP to HTTPS
  * @param metricsFilter A demonstration filter that adds timing information to
  * each response.
  */
@Singleton
class ProdFilters @Inject() (tlsFilter: TLSFilter, metricsFilter: MetricsFilter)
  extends DefaultHttpFilters(tlsFilter, metricsFilter)