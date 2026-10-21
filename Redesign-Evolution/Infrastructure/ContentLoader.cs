using System.Text.Json;
using System.Text.Json.Serialization;
using Redesign.Core;

namespace Redesign.Infrastructure;

public static class ContentLoader
{
    private static readonly JsonSerializerOptions Options = new()
    {
        PropertyNameCaseInsensitive = true,
        UnmappedMemberHandling = JsonUnmappedMemberHandling.Disallow,
        Converters = { new JsonStringEnumConverter(allowIntegerValues: false) }
    };
    public static GameDefinition Parse(string json)
    {
        var result = JsonSerializer.Deserialize<GameDefinition>(json, Options)
            ?? throw new ArgumentException("Content is empty.");
        result.Validate();
        return result;
    }
}
