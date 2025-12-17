import java.util.ArrayList;

public class TriggerCondition {

    public CorrectiveCallsSet correctiveCallsSet;
    public ArrayList<Boolean> isNegated;
    public ArrayList<ErrorCondition> errorConditions;
    public ArrayList<String> logicalOperator;

    // if multiple error conditions, need to store them and logical operator

    TriggerCondition(CorrectiveCallsSet correctiveCallsSet, ArrayList<Boolean> isNegated,
                     ArrayList<ErrorCondition> errorConditions, ArrayList<String> logicalOperator) {
        // logicalOperators are used to join multiple error conditions. If there's only 1 errorCondition, then there
        // are no logicalOperators.
        if (errorConditions.size() == 1) {
            this.correctiveCallsSet = correctiveCallsSet;
            this.isNegated = isNegated;
            this.errorConditions = errorConditions;
            this.logicalOperator = null;
        }
        else {
            this.correctiveCallsSet = correctiveCallsSet;
            this.isNegated = isNegated;
            this.errorConditions = errorConditions;
            this.logicalOperator = logicalOperator;
        }
    }
}
