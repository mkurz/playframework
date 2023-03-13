/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

package play.api.routing

import play.api.mvc.RequestHeader

/**
 * The Play "String Interpolating Routing DSL", sird for short.
 *
 * This provides:
 * - Extractors for requests that extract requests by method, eg GET, POST etc.
 * - A string interpolating path extractor
 * - Extractors for binding parameters from paths to various types, eg int, long, double, bool.
 *
 * The request method extractors return the original request for further extraction.
 *
 * The path extractor supports three kinds of extracted values:
 * - Path segment values. This is the default, eg `p"/foo/\$id"`. The value will be URI decoded, and may not traverse /'s.
 * - Full path values. This can be indicated by post fixing the value with a *, eg `p"/assets/\$path*"`. The value will
 *   not be URI decoded, as that will make it impossible to distinguish between / and %2F.
 * - Regex values. This can be indicated by post fixing the value with a regular expression enclosed in angle brackets.
 *   For example, `p"/foo/\$id<[0-9]+>`. The value will not be URI decoded.
 *
 * The extractors for primitive types are merely provided for convenience, for example, `p"/foo/\${int(id)}"` will
 * extract `id` as an integer.  If `id` is not an integer, the match will simply fail.
 *
 * Example usage:
 *
 * {{{
 *  import play.api.routing.sird._
 *  import play.api.routing._
 *  import play.api.mvc._
 *
 *  Router.from {
 *    case GET(p"/hello/\$to") => Action {
 *      Results.Ok(s"Hello \$to")
 *    }
 *    case PUT(p"/api/items/\${int(id)}") => Action.async { req =>
 *      Items.save(id, req.body.json.as[Item]).map { _ =>
 *        Results.Ok(s"Saved item \$id")
 *      }
 *    }
 *  }
 * }}}
 */
package object sird extends RequestMethodExtractors with PathBindableExtractors {
  implicit class UrlContext(sc: StringContext) {

    /**
     * String interpolator for extracting parameters out of URL paths.
     *
     * By default, any sub value extracted out by the interpolator will match a path segment, that is, any
     * String not containing a /, and its value will be decoded.  If however the sub value is suffixed with *,
     * then it will match any part of a path, and not be decoded.  Regular expressions are also supported, by
     * suffixing the sub value with a regular expression in angled brackets, and these are not decoded.
     */
    val p: PathExtractor = PathExtractor.cached(sc.parts)
  }

  /**
   *
   * See
   * https://github.com/lampepfl/dotty/issues/8577
   * https://github.com/lampepfl/dotty/pull/16358
   * -> which made it into Scala 3.3.0-RC1
   *
   * What I did here is basically the same like shown in the test the above PR added:
   * https://github.com/lampepfl/dotty/pull/16358/files#diff-7d4c46ab2cb40487d4e63816f21e5fc1edbc6fad5fa0945f93e5cd79fe8ead08
   *
   */

  extension(ctx: StringContext) {
    /**
     * String interpolator for required query parameters out of query strings.
     *
     * The format must match `q"paramName=\${param}"`.
     */
    def q: Macro.StrCtx = Macro(ctx)
//   def `q_?`: Macro.StrCtx = Macro(ctx)
//   def q_o: Macro.StrCtx = Macro(ctx)
//   ...
  }

  extension(inline sc: Macro.StrCtx) {
    inline def unapplySeq(rh: RequestHeader): Option[Seq[String]] = ${ macroimpl.QueryStringParameterMacros.required('sc, 'rh) }
    // Attempt to somehow handle everything with just one unapplySeq here:
    //inline def unapplySeq[R](rh: R): Option[Seq[_]] = ${ macroimpl.QueryStringParameterMacros.choose[R]('sc, 'rh) }
  }

  object Macro {
    // Can't use opaque here really:
    // From https://dotty.epfl.ch/docs/reference/other-new-features/opaques.html
    // * "..within the scope, it is treated as a type alias, but this is opaque to the outside world where,
    //    in consequence, .. is seen as an abstract type that has nothing to do with ..."
    // * "In general, one can think of an opaque type as being only transparent in the scope of private[this]
    //   (unless the type is a top level definition - in this case, it's transparent only within the file it's defined in)."
    // -> That means in QueryStringParameterMacros.scala StrCtx and StringContext seem to be completly other types, so we can't pattern match
    // From https://www.baeldung.com/scala/opaque-type-alias
    // * "Opaque types don’t support pattern matching"
    // -> Again problem for us
    /* opaque */ type StrCtx = StringContext
    def apply(ctx: StringContext): StrCtx = ctx
    def unapply(ctx: StrCtx): Option[StringContext] = Some(ctx)
  }

  /**
   * Allow multiple parameters to be extracted
   */
  object & {
    def unapply[A](a: A): Option[(A, A)] =
      Some((a, a))
  }

  /**
   * Same as &, but for convenience to make the dsl look nicer when extracting query strings
   */
  val ? = &

  /**
   * The query string type
   */
  type QueryString = Map[String, Seq[String]]
}
