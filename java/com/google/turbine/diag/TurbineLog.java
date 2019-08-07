/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.turbine.diag;

import com.google.common.collect.ImmutableList;
import com.google.turbine.diag.TurbineError.ErrorKind;
import java.util.LinkedHashSet;
import java.util.Set;

/** A log that collects diagnostics. */
public class TurbineLog {

  private final Set<TurbineDiagnostic> errors = new LinkedHashSet<>();

  public TurbineLogWithSource withSource(SourceFile source) {
    return new TurbineLogWithSource(source);
  }

  public void maybeThrow() {
    if (!errors.isEmpty()) {
      throw new TurbineError(ImmutableList.copyOf(errors));
    }
  }

  /**
   * Returns true if a non-deferrable error was raised during annotation processing, i.e. an error
   * reported by an annotation processor.
   *
   * <p>Errors reported by turbine (e.g. missing symbols) are non-fatal, since they may be fixed by
   * code generated in later processing rounds.
   */
  public boolean errorRaised() {
    for (TurbineDiagnostic error : errors) {
      switch (error.kind()) {
        case PROC:
          return true;
        default:
      }
    }
    return false;
  }

  /** Reset the log between annotation processing rounds. */
  public void clear() {
    errors.clear();
  }

  /**
   * Reports an annotation processing diagnostic.
   *
   * <p>This is a stop-gap until annotation processing diagnostic positions are implemented; see the
   * {@link javax.annotation.processing.Messager} implementation.
   */
  // TODO(cl/260804999): finish implementing AP diagnostics, especially position handling
  public void error(String message) {
    errors.add(TurbineDiagnostic.create(ErrorKind.PROC, message, ImmutableList.of()));
  }

  /** A log for a specific source file. */
  public class TurbineLogWithSource {

    private final SourceFile source;

    private TurbineLogWithSource(SourceFile source) {
      this.source = source;
    }

    public void error(ErrorKind kind, Object... args) {
      errors.add(TurbineDiagnostic.format(source, kind, args));
    }

    public void error(int position, ErrorKind kind, Object... args) {
      errors.add(TurbineDiagnostic.format(source, position, kind, args));
    }
  }
}
