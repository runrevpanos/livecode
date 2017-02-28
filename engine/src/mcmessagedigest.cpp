/*                                                                     -*-c++-*-
Copyright (C) 2017 LiveCode Ltd.

This file is part of LiveCode.

LiveCode is free software; you can redistribute it and/or modify it under
the terms of the GNU General Public License v3 as published by the Free
Software Foundation.

LiveCode is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
for more details.

You should have received a copy of the GNU General Public License
along with LiveCode.  If not see <http://www.gnu.org/licenses/>.  */

#include <algorithm>
#include <iterator>

#include "globals.h"
#include "md5.h"
#include "sha1.h"

/* ----------------------------------------------------------------
 * Helper functions
 * ---------------------------------------------------------------- */

/* Convert a span of bytes into a newly-allocated data ref */
static MCAutoDataRef data_new_from_bytes(MCSpan<byte_t> p_bytes)
{
    MCAutoDataRef t_data;
    MCDataCreateWithBytes(p_bytes.data(),
                          p_bytes.sizeBytes(),
                          &t_data);
    return t_data;
}

/* ----------------------------------------------------------------
 * Specific hash implementations
 * ---------------------------------------------------------------- */

/* All message digest hash implementations use the same API, varying
 * only in a few type definitions.  This template generates a function
 * that calls the necessary functions in the correct order. */
template <typename State, size_t DigestLength,
          typename Buffer, typename Length,
          void (*Init)(State*),
          void (*Update)(State*, const Buffer*, Length),
          void (*Finish)(State*, byte_t*)>
static MCAutoDataRef do_digest(MCDataRef p_data)
{
    State state;
    /* Some hash functions copy data into their output buffer in
     * 64-bit chunks, even if the digest length isn't a multiple of 16
     * bytes.  Make sure the digest buffer is always extended to hold
     * a round number of 64-bit chunks. */
    int align_bits = sizeof(uint64_t) - 1;
    byte_t digest[(DigestLength + align_bits) & ~align_bits];
    Init(&state);
    Update(&state, MCDataGetBytePtr(p_data), MCDataGetLength(p_data));
    Finish(&state, digest);

    return data_new_from_bytes({digest, DigestLength});
}

static MCAutoDataRef md5_digest(MCDataRef p_data)
{
    return do_digest<md5_state_t, 16, byte_t, int,
                     md5_init, md5_append, md5_finish>(p_data);
}

static MCAutoDataRef sha1_digest(MCDataRef p_data)
{
    return do_digest<sha1_state_t, 20, void, uint32_t,
                     sha1_init, sha1_append, sha1_finish>(p_data);
}

/* ----------------------------------------------------------------
 * Generalised message digest function
 * ---------------------------------------------------------------- */

typedef MCAutoDataRef (*digest_func_t)(MCDataRef p_data);

struct digest_mapping_t
{
    const char* m_name;
    const digest_func_t m_digest_func;
};

static const digest_mapping_t k_digest_map[] =
{
    { "md5",      md5_digest  },
    { "sha-1",    sha1_digest },
};

/* Normalize a message digest name.  Currently, this is limited to
 * conversion to lowercase. */
static MCAutoStringRef
normalize_digest_name(MCNameRef p_digest_name)
{
    MCAutoStringRef t_string;
    if (!MCStringMutableCopy(MCNameGetString(p_digest_name), &t_string))
        return MCAutoStringRef();
    if (!MCStringLowercase(*t_string, kMCSystemLocale))
        return MCAutoStringRef();
    return t_string;
}

/* Generalized message digest
 *
 * Given a named digest function and a block of input data, computes
 * and returns the message digest of the input data as binary data. */
static MCAutoDataRef
MCFiltersMessageDigest(MCDataRef p_data,
                       MCNameRef p_digest_name)
{
    MCAutoStringRef t_digest_normalized = normalize_digest_name(p_digest_name);
    if (!t_digest_normalized.IsSet()) return MCAutoDataRef();

    MCAutoStringRefAsCString t_digest_chars;
    if (!t_digest_chars.Lock(*t_digest_normalized)) return MCAutoDataRef();

    auto t_mapping =
        std::find_if(std::begin(k_digest_map), std::end(k_digest_map),
                     [&](const digest_mapping_t& p_mapping) {
                         return 0 == strcmp(p_mapping.m_name, *t_digest_chars);
                     });
    if (t_mapping == std::end(k_digest_map))
    {
        /* No known message digest algorithm of this name */
        /* TODO[2017-02-28] Failing to find a matching algorithm should
         * throw a helpful error. */
        return MCAutoDataRef();
    }

    return t_mapping->m_digest_func(p_data);
}

/* ----------------------------------------------------------------
 * Exec function adaptors
 * ---------------------------------------------------------------- */

static void
filters_result(MCExecContext& ctxt,
               MCAutoDataRef&& p_digest,
               MCDataRef& r_digest)
{
    if (p_digest.IsSet())
        r_digest = p_digest.Take();
    else
        ctxt.Throw();
}

void
MCFiltersEvalMessageDigest(MCExecContext& ctxt,
                           MCDataRef p_src,
                           MCNameRef p_digest_name,
                           MCDataRef& r_digest)
{
    filters_result(ctxt, MCFiltersMessageDigest(p_src, p_digest_name), r_digest);
}

void
MCFiltersEvalMD5Digest(MCExecContext& ctxt,
                       MCDataRef p_src,
                       MCDataRef& r_digest)
{
    filters_result(ctxt, md5_digest(p_src), r_digest);
}

void
MCFiltersEvalSHA1Digest(MCExecContext& ctxt,
                        MCDataRef p_src,
                        MCDataRef& r_digest)
{
    filters_result(ctxt, sha1_digest(p_src), r_digest);
}
