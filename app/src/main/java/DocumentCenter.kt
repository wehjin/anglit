import android.content.Context
import rx.Observable
import rx.schedulers.Schedulers

/**
 * @author Jeffrey Yu
 * @since 12/11/16.
 */

object DocumentCenter {
    lateinit var context: Context

    fun enable(context: Context) {
        this.context = context
    }

    fun deleteDocument(documentId: String?) {
        documentId ?: return
        Observable.just(documentId)
                .doOnNext { context.deleteFile(it) }
                .onErrorResumeNext { Observable.empty() }
                .subscribeOn(Schedulers.io())
                .subscribe()
    }
}