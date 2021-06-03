package com.amplifyframework.datastore.generated.model;

import com.amplifyframework.core.model.temporal.Temporal;

import java.util.List;
import java.util.UUID;
import java.util.Objects;

import androidx.core.util.ObjectsCompat;

import com.amplifyframework.core.model.Model;
import com.amplifyframework.core.model.annotations.Index;
import com.amplifyframework.core.model.annotations.ModelConfig;
import com.amplifyframework.core.model.annotations.ModelField;
import com.amplifyframework.core.model.query.predicate.QueryField;

import static com.amplifyframework.core.model.query.predicate.QueryField.field;

/** This is an auto generated class representing the Route type in your schema. */
@SuppressWarnings("all")
@ModelConfig(pluralName = "Routes")
public final class Route implements Model {
  public static final QueryField ID = field("Route", "id");
  public static final QueryField DESTINATION = field("Route", "destination");
  public static final QueryField STOPS = field("Route", "stops");
  private final @ModelField(targetType="ID", isRequired = true) String id;
  private final @ModelField(targetType="String", isRequired = true) String destination;
  private final @ModelField(targetType="String", isRequired = true) List<String> stops;
  private @ModelField(targetType="AWSDateTime", isReadOnly = true) Temporal.DateTime createdAt;
  private @ModelField(targetType="AWSDateTime", isReadOnly = true) Temporal.DateTime updatedAt;
  public String getId() {
      return id;
  }
  
  public String getDestination() {
      return destination;
  }
  
  public List<String> getStops() {
      return stops;
  }
  
  public Temporal.DateTime getCreatedAt() {
      return createdAt;
  }
  
  public Temporal.DateTime getUpdatedAt() {
      return updatedAt;
  }
  
  private Route(String id, String destination, List<String> stops) {
    this.id = id;
    this.destination = destination;
    this.stops = stops;
  }
  
  @Override
   public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if(obj == null || getClass() != obj.getClass()) {
        return false;
      } else {
      Route route = (Route) obj;
      return ObjectsCompat.equals(getId(), route.getId()) &&
              ObjectsCompat.equals(getDestination(), route.getDestination()) &&
              ObjectsCompat.equals(getStops(), route.getStops()) &&
              ObjectsCompat.equals(getCreatedAt(), route.getCreatedAt()) &&
              ObjectsCompat.equals(getUpdatedAt(), route.getUpdatedAt());
      }
  }
  
  @Override
   public int hashCode() {
    return new StringBuilder()
      .append(getId())
      .append(getDestination())
      .append(getStops())
      .append(getCreatedAt())
      .append(getUpdatedAt())
      .toString()
      .hashCode();
  }
  
  @Override
   public String toString() {
    return new StringBuilder()
      .append("Route {")
      .append("id=" + String.valueOf(getId()) + ", ")
      .append("destination=" + String.valueOf(getDestination()) + ", ")
      .append("stops=" + String.valueOf(getStops()) + ", ")
      .append("createdAt=" + String.valueOf(getCreatedAt()) + ", ")
      .append("updatedAt=" + String.valueOf(getUpdatedAt()))
      .append("}")
      .toString();
  }
  
  public static DestinationStep builder() {
      return new Builder();
  }
  
  /** 
   * WARNING: This method should not be used to build an instance of this object for a CREATE mutation.
   * This is a convenience method to return an instance of the object with only its ID populated
   * to be used in the context of a parameter in a delete mutation or referencing a foreign key
   * in a relationship.
   * @param id the id of the existing item this instance will represent
   * @return an instance of this model with only ID populated
   * @throws IllegalArgumentException Checks that ID is in the proper format
   */
  public static Route justId(String id) {
    try {
      UUID.fromString(id); // Check that ID is in the UUID format - if not an exception is thrown
    } catch (Exception exception) {
      throw new IllegalArgumentException(
              "Model IDs must be unique in the format of UUID. This method is for creating instances " +
              "of an existing object with only its ID field for sending as a mutation parameter. When " +
              "creating a new object, use the standard builder method and leave the ID field blank."
      );
    }
    return new Route(
      id,
      null,
      null
    );
  }
  
  public CopyOfBuilder copyOfBuilder() {
    return new CopyOfBuilder(id,
      destination,
      stops);
  }
  public interface DestinationStep {
    StopsStep destination(String destination);
  }
  

  public interface StopsStep {
    BuildStep stops(List<String> stops);
  }
  

  public interface BuildStep {
    Route build();
    BuildStep id(String id) throws IllegalArgumentException;
  }
  

  public static class Builder implements DestinationStep, StopsStep, BuildStep {
    private String id;
    private String destination;
    private List<String> stops;
    @Override
     public Route build() {
        String id = this.id != null ? this.id : UUID.randomUUID().toString();
        
        return new Route(
          id,
          destination,
          stops);
    }
    
    @Override
     public StopsStep destination(String destination) {
        Objects.requireNonNull(destination);
        this.destination = destination;
        return this;
    }
    
    @Override
     public BuildStep stops(List<String> stops) {
        Objects.requireNonNull(stops);
        this.stops = stops;
        return this;
    }
    
    /** 
     * WARNING: Do not set ID when creating a new object. Leave this blank and one will be auto generated for you.
     * This should only be set when referring to an already existing object.
     * @param id id
     * @return Current Builder instance, for fluent method chaining
     * @throws IllegalArgumentException Checks that ID is in the proper format
     */
    public BuildStep id(String id) throws IllegalArgumentException {
        this.id = id;
        
        try {
            UUID.fromString(id); // Check that ID is in the UUID format - if not an exception is thrown
        } catch (Exception exception) {
          throw new IllegalArgumentException("Model IDs must be unique in the format of UUID.",
                    exception);
        }
        
        return this;
    }
  }
  

  public final class CopyOfBuilder extends Builder {
    private CopyOfBuilder(String id, String destination, List<String> stops) {
      super.id(id);
      super.destination(destination)
        .stops(stops);
    }
    
    @Override
     public CopyOfBuilder destination(String destination) {
      return (CopyOfBuilder) super.destination(destination);
    }
    
    @Override
     public CopyOfBuilder stops(List<String> stops) {
      return (CopyOfBuilder) super.stops(stops);
    }
  }
  
}
