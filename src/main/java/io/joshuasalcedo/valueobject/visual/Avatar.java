package io.joshuasalcedo.valueobject.visual;

import io.joshuasalcedo.valueobject.ValueObject;

import io.joshuasalcedo.valueobject.exception.ValueObjectException;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
public record Avatar(
        String value
) implements ValueObject, Comparable<Avatar> {
    static final String DEFAULT_AVATAR_URL =
            "https://upload.wikimedia.org/wikipedia/commons/2/2c/Default_pfp.svg";


    public Avatar {

	}

    public static Avatar create(String value){
        if( Avatar.isValid(value)){
            return new Avatar(value);
        }else{
            throw new ValueObjectException("Invalid avatar url");
        }
    }


    public static Avatar create(URL url){
        return new Avatar(url.toString());
    }

    public static Avatar of(String value){
        return create(value);
    }


    private static  boolean isValid(String value){
        try {
            URI.create(value).toURL();
            return true;
        }catch (Exception _){
            return false;
        }
    }

    public URL toURL(){
        try {
            return URI.create(value).toURL();

        }catch (MalformedURLException e){
            throw new ValueObjectException(e);
        }
    }

    public static Avatar createDefault(){
        return new Avatar(DEFAULT_AVATAR_URL);
    }

    @Override
    public String toString() {
        return String.format("'avatar' : '%s'", value);
    }

    /**
     * Compares this Avatar to another Avatar by URL string.
     *
     * @param other the Avatar to compare to
     * @return negative if this comes before, zero if equal, positive if after
     */
    @Override
    public int compareTo(Avatar other) {
        return this.value.compareTo(other.value);
    }
}
