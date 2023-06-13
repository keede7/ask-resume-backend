package app.askresume.domain.submit.model

import app.askresume.domain.common.BaseTimeEntity
import app.askresume.domain.generative.interview.model.ResultInterviewMaker
import app.askresume.domain.submit.constant.SubmitDataStatus
import com.vladmihalcea.hibernate.type.json.JsonType
import org.hibernate.annotations.Comment
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import javax.persistence.*

@TypeDef(name = "json", typeClass = JsonType::class)
@Entity
class SubmitData(
    parameter: HashMap<String, Any>,
) : BaseTimeEntity() {

    @Comment("데이터")
    @Type(type = "json")
    @Column(columnDefinition = "json")
    var parameter: HashMap<String, Any> = parameter
        protected set

    @Comment("시도 횟수")
    @Column(nullable = false)
    var attempts: Int = 0
        protected set

    @Enumerated(EnumType.STRING)
    @Comment("상태")
    @Column(name = "status", nullable = false, length = 30)
    var submitDataStatus: SubmitDataStatus = SubmitDataStatus.WAITING
        protected set


    @OneToMany(cascade = [CascadeType.ALL])
    @JoinColumn(name = "submit_data_id")
    var resultInterviewMakerList: MutableList<ResultInterviewMaker> = mutableListOf()
        protected set
}