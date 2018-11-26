import com.dvoroncov.arcore.data.models.AnchorModel
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

// Use object so we have a singleton instance
object RxBus {
    private val publisher = PublishSubject.create<Any>()
    fun publish(event: Any) {
        publisher.onNext(event)
    }

    fun <T> listen(eventType: Class<T>): Observable<T> = publisher.ofType(eventType)

    class AddNewAnchor(val anchorModel: AnchorModel)
}
