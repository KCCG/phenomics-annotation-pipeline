package au.org.garvan.kccg.annotations.pipeline.engine.managers;

import au.org.garvan.kccg.annotations.pipeline.engine.annotators.Utilities;
import au.org.garvan.kccg.annotations.pipeline.engine.connectors.NotificationConnector;
import au.org.garvan.kccg.annotations.pipeline.engine.enums.AnnotationType;
import au.org.garvan.kccg.annotations.pipeline.model.feedback.inward.Feedback;
import au.org.garvan.kccg.annotations.pipeline.model.feedback.inward.FeedbackAnnotationItem;
import au.org.garvan.kccg.annotations.pipeline.model.feedback.outward.FeedbackAnnotationItemDetailed;
import au.org.garvan.kccg.annotations.pipeline.model.feedback.outward.FeedbackRequest;
import au.org.garvan.kccg.annotations.pipeline.model.query.ConceptFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Created by ahmed on 22/11/17.
 */

@Service
public class FeedbackManager {
    private final Logger slf4jLogger = LoggerFactory.getLogger(FeedbackManager.class);

    @Autowired
    NotificationConnector notificationConnector;

    public void init(){
        slf4jLogger.info(String.format("FeedbackRequest Manager init() called."));
    }

    public boolean processFeedback(Feedback feedback, String senderAddress)
    {
        slf4jLogger.info(String.format("FeedbackRequest received:%s. from:%s",feedback.toString(), senderAddress));

        //Prepare base request
        FeedbackRequest feedbackRequest = new FeedbackRequest(feedback);

        //Get annotation additional details.
        ConceptFilter conceptFilter =  Utilities.getFilterBasedOnId(feedbackRequest.getAnnotation().getId());

        //Fill in the blanks
        feedbackRequest.getAnnotation().setText(conceptFilter.getText());
        feedbackRequest.getAnnotation().setType(conceptFilter.getType());
        feedbackRequest.setTimeStamp(LocalDateTime.now().toString());
        feedbackRequest.setSendersAddress(senderAddress);

        notificationConnector.sendFeedback(feedbackRequest);

        return true;

    }

}
