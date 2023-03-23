package parse.dont.validate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.With;

@With
@Getter
@AllArgsConstructor
public class NIRBuilder {
    private final Sex sex;
    private Year year;

    public NIRBuilder(Sex sex) {
        this.sex = sex;
    }
}
